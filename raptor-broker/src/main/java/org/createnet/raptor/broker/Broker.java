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
package org.createnet.raptor.broker;

import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.createnet.raptor.models.configuration.BrokerConfiguration;
import org.createnet.raptor.models.configuration.RaptorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class Broker {

    final protected Logger logger = LoggerFactory.getLogger(Broker.class);
    private final RaptorConfiguration config;
    
    @Bean
    RaptorSecurityManager raptorSecurityManager() {
        return new RaptorSecurityManager();
    }    

    public Broker(RaptorConfiguration config) {
        this.config = config;
    }
    
    public class BrokerException extends RuntimeException {

        public BrokerException(Throwable t) {
            super(t);
        }
    }

    final protected EmbeddedActiveMQ server = new EmbeddedActiveMQ();

    public void initialize() {
        setupServer();
    }

    protected void setupServer() {
        BrokerConfiguration brokerConfig = config.getBroker();
        server.setSecurityManager(raptorSecurityManager());
        server.setConfigResourcePath(brokerConfig.getArtemis());
    }
    
    public void start() {

        try {
            
            logger.debug("Initializing broker services");
            initialize();            
            
            logger.debug("Starting broker");
            
            server.start();
        } catch (Exception ex) {
            logger.error("Broker startup error: {}", ex.getMessage(), ex);
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

}
