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
package org.createnet.raptor.broker.security;

import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.security.cert.X509Certificate;
import org.apache.activemq.artemis.core.security.CheckType;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.spi.core.protocol.RemotingConnection;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager2;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.http.exception.ConfigurationException;
import org.createnet.raptor.http.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class RaptorSecurityManager implements ActiveMQSecurityManager2 {
  
  protected enum Roles {
    user, admin
  }
  
  private final Logger logger = LoggerFactory.getLogger(RaptorSecurityManager.class);
  
  @Inject
  AuthService auth;
  
  protected Authentication.UserInfo getUser(String token) {
    try {
      
      Authentication.UserInfo user = auth.getUser(token);
      if(user != null) {
        logger.debug("Authenticated user {}", user.getUserId());
        return user;
      }
      
    } catch (ConfigurationException | Authentication.AuthenticationException e) {
      logger.error("Authentication failed", e);
    }
    return null;
  }
  
  @Override
  public boolean validateUser(String user, String password, X509Certificate[] certificates) {
    logger.debug("Authenticate user {} with token {}", user, password);
    return (getUser(password) != null);
  }

  @Override
  public boolean validateUserAndRole(String username, String password, Set<Role> roles, CheckType checkType, String address, RemotingConnection connection) {
    
    logger.debug("Authenticate user {} with token {} and roles {} on topic {}", username, password, roles, address);
    
    Authentication.UserInfo user = getUser(password);
    
    if(
        address.contains("$sys.mqtt.queue.qos2") ||
            address.contains("$sys.mqtt.#")
      ) {
      switch(checkType) {
        
        case CREATE_DURABLE_QUEUE:
        case DELETE_DURABLE_QUEUE:
          
        case CREATE_NON_DURABLE_QUEUE:
        case DELETE_NON_DURABLE_QUEUE:
          
        case CONSUME:
        case SEND:
          
          return true;
        case MANAGE:
          if(user.getRoles().contains(Roles.admin.name()))
            return true;
        default:
          return false;
      }
    }
    
    String[] topicTokens = address.split("\\.");
    if(topicTokens.length > 2) {
      
      String objectId = topicTokens[2];
      
      try {
        UUID.fromString(objectId);
      }
      catch(IllegalArgumentException e) {
        return false;
      }
      
      try {
        logger.debug("Check access permission of user {} to object {}", user.getUserId(), objectId);        
        boolean allowed = auth.isAllowed(user.getAccessToken(), objectId, Authorization.Permission.Subscribe);
        return allowed;
      } catch (Authorization.AuthorizationException ex) {
        logger.error("Failed to subscribe", ex);
        return false;
      }
    }
    
    return false;
  }

  @Override
  public boolean validateUser(String user, String password) {
    logger.debug("validateUser(user, password): NOT IMPLEMENTED");
//    logger.debug("Authenticate user {} with token {}", user, password);
    return true;
  }

  @Override
  public boolean validateUserAndRole(String user, String password, Set<Role> roles, CheckType checkType) {
    logger.debug("validateUserAndRole(user, password, roles, checkType): NOT IMPLEMENTED");
//    logger.warn("Authenticate user {} with token {} and roles {} on {}", user, password, roles, checkType);
    return false;    
  }
  
}
