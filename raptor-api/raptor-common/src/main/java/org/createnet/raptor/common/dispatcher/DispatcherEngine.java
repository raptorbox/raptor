/*
 * Copyright 2017 FBK/CREATE-NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.createnet.raptor.common.dispatcher;

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
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class DispatcherEngine {

    final protected Logger logger = LoggerFactory.getLogger(DispatcherEngine.class);

    protected BlockingQueue<Runnable> queue;
    protected Queue messageQueue = new Queue();

    protected org.createnet.raptor.models.configuration.DispatcherConfiguration config;
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

    public void initialize(org.createnet.raptor.models.configuration.DispatcherConfiguration config) {

        this.config = config;

        queue = new LinkedBlockingQueue(100);

        executorService = new ThreadPoolExecutor(5, 15, 1000, TimeUnit.MILLISECONDS, queue, new ThreadPoolExecutor.CallerRunsPolicy());
        executorService.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor1) {
                try {
                    logger.warn("Message delivery failed, add back to queue");
                    Thread.sleep(500);
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
        if (messageQueue.size() > config.getQueueLength()) {
            logger.warn("Message queue limit reached ({})", config.getQueueLength());
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
            logger.error("Message dropped [topic={}]", qm.topic);
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
