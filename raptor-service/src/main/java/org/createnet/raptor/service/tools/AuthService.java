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
package org.createnet.raptor.service.tools;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import org.createnet.raptor.auth.AuthProvider;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authentication.Authentication.UserInfo;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.models.objects.Device;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Singleton
@Service
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

  synchronized protected AuthProvider getProvider()  {

    if (auth == null) {
      auth = new AuthProvider();
      auth.initialize(config.getAuth());
    }

    return auth;
  }

  public boolean isAllowed(String accessToken, Device obj, Authorization.Permission op) {
    return getProvider().isAuthorized(accessToken, obj, op);
  }

  public boolean isAllowed(Device obj, Authorization.Permission op) {
    return getProvider().isAuthorized(getAccessToken(), obj, op);
  }

  public boolean isAllowed(Authorization.Permission op)  {
    return isAllowed(null, op);
  }

  public UserInfo getUser()  {
    return getProvider().getUser(getAccessToken());
  }

  public UserInfo getUser(String accessToken)  {
    return getProvider().getUser(accessToken);
  }

  public UserInfo login(String username, String password)  {
    return getProvider().login(username, password);
  }

  public String getAccessToken() {
    if (securityContext == null) {
      return null;
    }
    if (securityContext.getUserPrincipal() == null) {
      return null;
    }

    return securityContext.getUserPrincipal().getName();
  }

  public void sync(String token, Device obj, Authentication.SyncOperation op) {
    getProvider().sync(token, obj, op);
  }

}
