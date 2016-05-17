/*
 * Copyright 2016 Luca Capra <lcapra@create-net.org>.
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

import java.util.logging.Level;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.createnet.raptor.broker.security.RaptorLoginModule;
import org.createnet.raptor.broker.security.RaptorSecurityManager;
import org.createnet.raptor.http.ApplicationConfig;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class Broker {
  
  final protected Logger logger = LoggerFactory.getLogger(RaptorLoginModule.class);
  
  public static void main(final String[] args) throws BrokerException {
    
    final Broker broker = new Broker();
    broker.initialize();

    broker.start();
    
  }
  
  public class BrokerException extends Exception {
    public BrokerException(Throwable t) {
      super(t);
    }
  }
  
  final protected EmbeddedActiveMQ server = new EmbeddedActiveMQ();
  final RaptorSecurityManager securityManager = new RaptorSecurityManager();
  
  public Broker() {}

  public void initialize() {
    
    ServiceLocatorFactory locatorFactory = ServiceLocatorFactory.getInstance();
    ServiceLocator serviceLocator = locatorFactory.create("BrokerLocator");
    ServiceLocatorUtilities.bind(serviceLocator, new ApplicationConfig.AppBinder());

    serviceLocator.inject(securityManager);
  }
  
  public void start() throws BrokerException {
    
    server.setSecurityManager(securityManager);
    try {
      logger.debug("Starting broker");
      server.start();
    } catch (Exception ex) {
      throw new BrokerException(ex);
    }
  }
  
  public void stop() throws BrokerException {
    
    server.setSecurityManager(securityManager);
    try {
      logger.debug("Stopping broker");
      server.stop();
    } catch (Exception ex) {
      throw new BrokerException(ex);
    }
  }
  
}
