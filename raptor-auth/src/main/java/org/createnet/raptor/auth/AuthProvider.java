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
package org.createnet.raptor.auth;

import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authentication.impl.AllowAllAuthentication;
import org.createnet.raptor.auth.authentication.impl.TokenAuthentication;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.auth.authorization.impl.AllowAllAuthorization;
import org.createnet.raptor.auth.authorization.impl.TokenAuthorization;
import org.createnet.raptor.auth.cache.AuthCache;
import org.createnet.raptor.auth.cache.impl.MemoryCache;
import org.createnet.raptor.auth.cache.impl.NoCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class AuthProvider implements Authorization, Authentication {

  protected final Logger logger = LoggerFactory.getLogger(AuthProvider.class);

  protected String accessToken;
  protected String userId;

  protected Authorization authorizationInstance;
  protected Authentication authenticationInstance;

  protected AuthCache cache;

  protected AuthConfiguration configuration;

  @Override
  public void initialize(AuthConfiguration configuration) {

    this.configuration = configuration;
    
    String cacheType = (String) configuration.cache;
    switch (cacheType) {
//      case "redis":
//        break;
      case "memory":
        cache = new MemoryCache();
        break;
      case "no_cache":
      default:
        cache = new NoCache();
        break;
    }

    String authType = (String) configuration.type;
    switch (authType) {
      case "token":
        authenticationInstance = new TokenAuthentication();
        authorizationInstance = new TokenAuthorization();
        break;
      case "allow_all":
      default:
        authenticationInstance = new AllowAllAuthentication();
        authorizationInstance = new AllowAllAuthorization();
        break;
    }
    
    cache.initialize(configuration);
    cache.setup();
    
    authenticationInstance.initialize(configuration);
    authorizationInstance.initialize(configuration);

  }

  @Override
  public boolean isAuthorized(String accessToken, String id, Permission op) throws AuthorizationException {
    
    try {
    
      UserInfo user = getUser(accessToken);
      
      Boolean cachedValue = cache.get(user.getUserId(), id, op);
      if(cachedValue != null) {
        logger.debug("Reusing permission cache for userId {} objectId {} permission {} = {}", user.getUserId(), id, op.name(), cachedValue);
        return cachedValue;
      }
      
      logger.debug("Requesting {} permission for {}", op, id);
      
      boolean isauthorized = authorizationInstance.isAuthorized(accessToken, id, op);
      
      cache.set(user.getUserId(), id, op, isauthorized);
      
      logger.debug("Permission check for userId {} objectId {} permission {} = {}", user.getUserId(), id, op.name(), isauthorized);
      
      return isauthorized;
      
    } catch (AuthCache.PermissionCacheException | AuthenticationException ex) {
      throw new AuthorizationException(ex);
    }
  }

  @Override
  public UserInfo getUser(String accessToken) throws AuthenticationException {
    
    if(accessToken == null) {
      throw new AuthenticationException("accessToken not provided");
    }
    
    try {

      UserInfo cachedValue = cache.get(accessToken);
      if(cachedValue != null) {
        logger.debug("Reusing cached user details for userId {}", cachedValue.getUserId());
        return cachedValue;
      }
      
    } catch (AuthCache.PermissionCacheException e) {
      logger.warn("Exception loading user cache for {}, query source auth system", accessToken);
    }
      
    logger.debug("Loading user details for token {}", accessToken);
    UserInfo user = authenticationInstance.getUser(accessToken);
    
    logger.debug("token ok, loaded user {}", user.getUserId());
    
    try {
      cache.set(user);
    } catch (AuthCache.PermissionCacheException ex) {
      logger.warn("Error storing cache for user {}", user.getUserId());
    }
    
    return user;
  }

  @Override
  public void sync() throws AuthenticationException {
    authenticationInstance.sync();
  }

}
