/*
 * The MIT License
 *
 * Copyright 2017 FBK/CREATE-NET
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.createnet.raptor.dispatcher;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class Dispatcher {

    public enum ObjectOperation {
        create, update, delete, push
    }

    public enum ActionOperation {
        execute, delete
    }

    final protected Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    protected BlockingQueue<Runnable> queue;
    protected Queue messageQueue = new Queue();

    protected DispatcherConfiguration config;
    protected BrokerClient client = new BrokerClient();

    final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);

    protected ThreadPoolExecutor executorService;

    class MessageDispatcher implements Runnable {

        final protected Queue.QueueMessage qm;
        final protected BrokerClient client;

        public MessageDispatcher(Queue.QueueMessage qm, BrokerClient client) {
            this.qm = qm;
            this.client = client;
        }

        public Queue.QueueMessage getQm() {
            return qm;
        }

        @Override
        public void run() {
            try {
                client.sendMessage(qm.topic, qm.message);
                logger.debug("Message sent to {}", qm.topic);
            } catch (DispatchException ex) {
                requeue(qm);
                logger.error("Error sending message: {}", ex.getMessage());
            }
        }

    }

    public void initialize(DispatcherConfiguration config) {

        this.config = config;

        queue = new LinkedBlockingQueue(100);

        executorService = new ThreadPoolExecutor(5, 15, 1000, TimeUnit.MILLISECONDS, queue, new ThreadPoolExecutor.CallerRunsPolicy());
        executorService.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor1) {
                try {
                    logger.warn("Message delivery failed, add back to queue");
                    Thread.sleep(1000);
                    requeue(((MessageDispatcher) r).getQm());
                } catch (InterruptedException e) {

                }
            }
        });

        client.initialize(config);

        try {
            client.getConnection();
        } catch (MqttException ex) {
            logger.error("Cannot connect to the broker", ex);
        }

        executorService.prestartAllCoreThreads();

        scheduledExecutor.scheduleAtFixedRate(() -> {
            dispatch();
        }, 0, 100, TimeUnit.MILLISECONDS);

    }

    public void add(String topic, String message) {
        Queue.QueueMessage qm = new Queue.QueueMessage(topic, message);
        add(qm);
    }

    public void add(Queue.QueueMessage qm) {
        if (messageQueue.size() > config.queueLength) {
            logger.warn("Message queue limit reached ({})", config.queueLength);
        }
        messageQueue.add(qm);
        dispatch();
    }

    public int size() {
        return messageQueue.size();
    }

    protected void requeue(Queue.QueueMessage qm) {
        qm.tries++;
        if (qm.valid()) {
            logger.debug("Message added back to queue due to dispatcher error: {}/{}", qm.tries, qm.maxRetries);
            add(qm);
        } else {
            logger.debug("Message dropped");
        }
    }

    public void dispatch() {

        if (messageQueue.size() == 0) {
            return;
        }

        logger.debug("Message queue has {} records", messageQueue.size());

        Queue.QueueMessage qm = messageQueue.pop();

        if (qm == null) {
            return;
        }

        executorService.execute(new MessageDispatcher(qm, client));
        dispatch();
    }

    protected void shutdown(ExecutorService executor) {
        try {
            logger.debug("Attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Tasks interrupted");
        } finally {
            if (!executor.isTerminated()) {
                logger.warn("cancel non-finished tasks");
            }
            executor.shutdownNow();
            logger.warn("Shutdown finished");
        }
    }

    public void close() {
        shutdown(scheduledExecutor);
        shutdown(executorService);
        client.disconnect();
        logger.debug("Closed dispatcher");
    }

}
