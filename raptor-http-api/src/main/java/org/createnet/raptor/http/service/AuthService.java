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
package org.createnet.raptor.http.service;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import org.createnet.raptor.auth.AuthProvider;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authentication.Authentication.UserInfo;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.createnet.raptor.events.Event;
import org.createnet.raptor.http.events.ObjectEvent;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class AuthService {

  @Inject
  ConfigurationService config;
  @Inject
  EventEmitterService emitter;

  @Context
  SecurityContext securityContext;

  private final org.slf4j.Logger logger = LoggerFactory.getLogger(AuthService.class);

  public AuthService() {
  }

  private AuthProvider auth;

  protected AuthProvider getProvider() throws ConfigurationException {
    if (auth == null) {
      auth = new AuthProvider();
      auth.initialize(config.getAuth());
    }
    
    initialize();
    
    return auth;
  }

  public boolean isAllowed(String accessToken, String id, Authorization.Permission op) throws Authorization.AuthorizationException, ConfigurationException {
    return getProvider().isAuthorized(accessToken, id, op);
  }

  public boolean isAllowed(String id, Authorization.Permission op) throws Authorization.AuthorizationException, ConfigurationException {
    return getProvider().isAuthorized(getAccessToken(), id, op);
  }

  public boolean isAllowed(Authorization.Permission op) throws Authorization.AuthorizationException, ConfigurationException {
    return isAllowed(null, op);
  }

  public UserInfo getUser() throws ConfigurationException, Authentication.AuthenticationException {
    return getProvider().getUser(getAccessToken());
  }

  public UserInfo getUser(String accessToken) throws ConfigurationException, Authentication.AuthenticationException {
    return getProvider().getUser(accessToken);
  }

  public String getAccessToken() {
    if (securityContext == null) {
      return null;
    }
    return securityContext.getUserPrincipal().getName();
  }

  private void initialize() {

    emitter.on(EventEmitterService.EventName.object, (Event event) -> {
      try {
        ObjectEvent objEvent = (ObjectEvent) event;
        getProvider().sync(objEvent.getAccessToken(), objEvent.getObject().id);
      } catch (Authentication.AuthenticationException | ConfigurationException ex) {
        logger.error("Event trigger exception", ex);
      }
    });

  }

}
