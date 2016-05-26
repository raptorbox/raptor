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
package org.createnet.raptor.broker.interceptors;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.core.protocol.stomp.StompFrame;
import org.apache.activemq.artemis.core.protocol.stomp.StompFrameInterceptor;
import org.apache.activemq.artemis.spi.core.protocol.RemotingConnection;
import org.createnet.raptor.broker.util.TopicChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class StompInterceptor implements StompFrameInterceptor {

  private final Logger logger = LoggerFactory.getLogger(StompInterceptor.class);
  final private TopicChecker topicChecker = new TopicChecker();

  @Override
  public boolean intercept(StompFrame packet, RemotingConnection connection) throws ActiveMQException {

//    logger.debug("Intercepted command {}", packet.getCommand());
//
//    switch (packet.getCommand()) {
//      case "SUBSCRIBE":
//      case "SEND":
//        
//        String[] destination = packet.getHeader("destination").split("\\.");
//        String objectId = destination[0];
//        boolean validUUID = topicChecker.checkUUID(objectId);
//        if (!validUUID) {
//          logger.debug("Object id length mismatch ({})", objectId.length());
//          return false;
//        }
//        
//        packet.getHeadersMap().put("destination", "jms.queue." + packet.getHeader("destination"));
//
//        break;
//    }

    return true;
  }

}
