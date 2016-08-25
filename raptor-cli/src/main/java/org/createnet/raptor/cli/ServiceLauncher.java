/*
 * Copyright 2016 CREATE-NET
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
package org.createnet.raptor.cli;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.createnet.raptor.broker.Broker;
import org.createnet.raptor.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ServiceLauncher {

  final private Logger logger = LoggerFactory.getLogger(ServiceLauncher.class);
  final private ExecutorService executor = Executors.newFixedThreadPool(2);
  
  protected boolean shuttingDown = false;

  protected Future<?> brokerTask;
  protected Future<?> httpTask;
  
  void start() {

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        logger.debug("Shutting down!");
        shuttingDown = true;
        brokerTask.cancel(true);
        httpTask.cancel(true);
      }
    });

    launch();
  }

  private void relaunch() {
    
    if(shuttingDown) return;
    
    try {
      executor.shutdownNow();
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      logger.error("Interruption detected, exit.");
      System.exit(1);
    } catch (Exception e) {
      logger.error("Exception on relaunch", e);
    }

    launch();
  }

  private void launch() {
    
    try {

      logger.info("Starting job for HTTPService");
      
      httpTask = executor.submit(() -> {
        HttpService http = new HttpService();
        try {
          http.start();
        } catch (Exception ex) {
          logger.error("Error starting http service: {}", ex.getMessage(), ex);
          Thread.currentThread().interrupt();
        }
      });

      logger.info("Starting job for Broker");

      brokerTask = executor.submit(() -> {
        Broker broker = new Broker();
        try {
          broker.initialize();
          broker.start();
        } catch (Exception ex) {
          logger.error("Error starting broker service: {}", ex.getMessage());
          Thread.currentThread().interrupt();
        }
      });
      
    } catch (Exception ex) {
      logger.error("Exception received, restarting services. {}", ex.getMessage());
      relaunch();
    }
  }

}
