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
package org.createnet.raptor.broker;

import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.createnet.raptor.broker.configuration.BrokerConfiguration;
import org.createnet.raptor.broker.security.RaptorSecurityManager;
import org.createnet.raptor.config.ConfigurationLoader;
import org.createnet.raptor.service.RaptorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class Broker {

    final protected Logger logger = LoggerFactory.getLogger(Broker.class);

    public static void main(final String[] args) {

        final Broker broker = new Broker();
        broker.initialize();

        broker.start();

    }

    private BrokerConfiguration brokerConfiguration;

    public class BrokerException extends RuntimeException {

        public BrokerException(Throwable t) {
            super(t);
        }
    }

    final protected EmbeddedActiveMQ server = new EmbeddedActiveMQ();
    final RaptorSecurityManager securityManager = new RaptorSecurityManager();

    public Broker() {
    }

    public void initialize() {

        RaptorService.inject(securityManager);
        setupServer();
    }

    protected void setupServer() {

        securityManager.setBrokerConfiguration(getBrokerConfiguration());

        server.setSecurityManager(securityManager);
        server.setConfigResourcePath(getConfigPath());
    }

    public void start() {

        try {
            logger.debug("Starting broker");
            server.start();
        } catch (Exception ex) {
            logger.error("Error launching the borker: {}", ex.getMessage(), ex);
            throw new BrokerException(ex);
        }
    }

    public void stop() {

        try {
            logger.debug("Stopping broker");
            server.stop();
        } catch (Exception ex) {
            throw new BrokerException(ex);
        }
    }

    private BrokerConfiguration getBrokerConfiguration() {

        if (brokerConfiguration == null) {
            brokerConfiguration = (BrokerConfiguration) ConfigurationLoader.getConfiguration("broker", BrokerConfiguration.class);
        }

        return brokerConfiguration;
    }

    private String getConfigPath() {

        if (brokerConfiguration.artemisConfiguration == null) {
            throw new RuntimeException("broker.xml path not specified");
        }

        return brokerConfiguration.artemisConfiguration;
    }

}
