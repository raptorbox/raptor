/*
 * The MIT License
 *
 * Copyright 2016 Luca Capra <lcapra@create-net.org>.
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class Dispatcher {

  final protected Logger logger = LoggerFactory.getLogger(Dispatcher.class);

  final protected int poolSize = 20;
  
  final protected int maxParallelTask = 8;
  final private AtomicInteger concurrentTasksCount = new AtomicInteger(0);  
  
  final protected int maxQueueLength = 1000000;

  final protected Map<String, String> config;
  final protected ExecutorService executorService;
  final protected Queue queue;
  final protected BrokerClient client;

  final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);

  public Dispatcher(Map<String, String> config) {

    this.config = config;
    
    executorService = Executors.newFixedThreadPool(poolSize);

    queue = new Queue();

    client = new BrokerClient(config);
    try {
      client.getConnection();
    } catch (MqttException ex) {
      logger.error("Cannot connect to the broker", ex);
    }

//
//    client.setListener(new BrokerClient.BrokerClientListener() {
//      @Override
//      public void onConnectSuccess() {
//      }
//
//      @Override
//      public void onConnectError(Throwable t) {
//        logger.debug("Error connecting", t);
//      }
//
//      @Override
//      public void onPublishSuccess() {
//        logger.debug("Message published");
//      }
//
//      @Override
//      public void onPublishError(Throwable t) {
//        logger.error("Error while publishing message", t);
//      }
//    });

    scheduledExecutor.scheduleAtFixedRate(() -> {
      dispatch();
//      logger.debug("Running timer, queue length {}", queue.size());
    }, 0, 100, TimeUnit.MILLISECONDS);

  }

  public void add(String topic, String message) {
    queue.add(new Queue.QueueMessage(topic, message));
    dispatch();
  }

  public int size() {
    dispatch();
    return queue.size();
  }

  protected void requeue(Queue.QueueMessage qm) {
    qm.tries++;
    if (qm.valid()) {
      logger.debug("Message added back to queue due to dispatcher error: {}/{}", qm.tries, qm.maxRetries);
      add(qm.topic, qm.message);
    } else {
      logger.debug("Message dropped");
    }

  }

  public void dispatch() {

    if (queue.size() == 0) {
//      logger.debug("Queue is empty");
      return;
    }

    if (!client.isConnected()) {
      logger.debug("Client not connected");
      if (queue.size() > maxQueueLength) {
        logger.error("Reached maximum queue length, flushin list");
        queue.clear();
      }
      return;
    }
    
    if(concurrentTasksCount.get() >= maxParallelTask) {
      logger.debug("Concurrent task limit reached {} of {}", concurrentTasksCount.get(), maxParallelTask);
      return;
    }
    
    logger.debug("Dispatching, {} items", queue.size());

    // pop out the list and send
    Queue.QueueMessage qm = queue.pop();

    try {
      
      poolAdd();

      executorService.execute(() -> {

        try {
          if (qm != null) {
            client.sendMessage(qm.topic, qm.message, new IMqttActionListener() {

              @Override
              public void onSuccess(IMqttToken imt) {
                poolRemove();
              }

              @Override
              public void onFailure(IMqttToken imt, Throwable thrwbl) {
                poolRemove();
                requeue(qm);
              }
            });
          }
        } catch (DispatchException ex) {
          poolRemove();
          requeue(qm);
        } finally {
          dispatch();
        }

      });

    } catch (Exception e) {
      logger.error("Executor exception", e);
    } finally {
      dispatch();
    }

  }
  
  private void poolAdd() {
    concurrentTasksCount.getAndIncrement();
  }
  
  private void poolRemove() {
    concurrentTasksCount.getAndDecrement();
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

  
  static public void main(String[] args) throws Exception {

    String topic = "foo/bar";
    String message = "hello world +";
    int times = 5000;

    ExecutorService exec = Executors.newFixedThreadPool(5);

    Map<String, String> config = new HashMap();
    config.put("uri", "tcp://servioticy.local:1883");
    config.put("username", "compose");
    config.put("password", "shines");

    Dispatcher instance = new Dispatcher(config);

    for (int i = 0; i < times; i++) {
      final String msg = message + i;
      exec.execute(() -> {
        instance.add(topic, msg);
      });
    }

//    exec.shutdown();
//    instance.close();
    System.out.println("Completed");
  }

}
