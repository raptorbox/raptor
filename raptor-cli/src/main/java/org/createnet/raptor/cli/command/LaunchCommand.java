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
package org.createnet.raptor.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import org.createnet.raptor.broker.Broker;
import org.createnet.raptor.http.HttpService;
import org.createnet.raptor.http.service.IndexerService;
import org.createnet.raptor.http.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Parameters(separators = "=", commandDescription = "Launch service(s)")
public class LaunchCommand implements Command {

    final private Logger logger = LoggerFactory.getLogger(LaunchCommand.class);
    final private ExecutorService executor = Executors.newFixedThreadPool(3);

    @Inject
    StorageService storage;

    @Inject
    IndexerService indexer;

    @Parameter(names = "--broker", description = "Launch the broker")
    public Boolean broker = false;

    @Parameter(names = "--http", description = "Launch the http api")
    public Boolean http = false;
    
    // this have deps problem, need to investigate
//    @Parameter(names = "--auth", description = "Launch the authentication api")
//    public Boolean auth = false;

    @Override
    public String getName() {
        return "launch";
    }

    @Override
    public void run() {

        boolean all = (
                !http 
                && !broker 
//                && !auth
                );

//        if (auth || all) {
//            executor.submit(() -> {
//                try {
//                    logger.info("Starting authentication service");
//                    SpringApplication.run(Application.class, new String[]{});
//                } catch (Exception ex) {
//                    logger.error("Error starting authentication service: {}", ex.getMessage(), ex);
//                    Thread.currentThread().interrupt();
//                }
//            });
//        }

        if (http || all) {
            executor.submit(() -> {
                HttpService http = new HttpService();
                try {
                    logger.info("Starting HTTP API");
                    http.start();
                } catch (Exception ex) {
                    logger.error("Error starting http service: {}", ex.getMessage(), ex);
                    Thread.currentThread().interrupt();
                }
            });
        }

        if (broker || all) {
            executor.submit(() -> {
                Broker broker = new Broker();
                try {
                    logger.info("Starting broker");
                    broker.initialize();
                    broker.start();
                } catch (Exception ex) {
                    logger.error("Error starting broker service: {}", ex.getMessage());
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

}
