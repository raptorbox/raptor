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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
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
public class RaptorAuth implements Authorization, Authentication {

  protected final Logger logger = LoggerFactory.getLogger(RaptorAuth.class);

  protected String accessToken;
  protected String userId;

  protected Authorization authorizationInstance;
  protected Authentication authenticationInstance;

  protected AuthCache cache;

  protected Map<String, Object> configuration;

  @Override
  public void initialize(Map<String, Object> configuration) {

    this.configuration = configuration;

    String cacheType = (String) configuration.getOrDefault("cache_type", "no_cache");
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

    String authType = (String) configuration.getOrDefault("type", "allow_all");
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
  public boolean isAuthorized(String id, Permission op) throws AuthorizationException {
    try {
      
      try {
        
        Boolean cachedValue = cache.get(getUserId(), id, op);
        if(cachedValue != null) {
          logger.debug("Reusing permission cache for {}.{}.{}", getUserId(), id, op.name());
          return cachedValue;
        }

      } catch (AuthCache.PermissionCacheException e) {
        logger.warn("Exception loading permission cache for {}, query source auth system", getUserId());
      }
      
      logger.debug("Requesting {} permission for {}", op, id);
      
      boolean isauthorized = authorizationInstance.isAuthorized(id, op);
      
      cache.set(getUserId(), id, op, isauthorized);
      
      return isauthorized;
      
    } catch (AuthCache.PermissionCacheException ex) {
      throw new AuthorizationException(ex);
    }
  }

  @Override
  public UserInfo getUser(String accessToken) throws AutenticationException {

    try {

      UserInfo cachedValue = cache.get(accessToken);
      if(cachedValue != null) {
        logger.debug("Reusing cached user details for {}", getUserId());
        return cachedValue;
      }
      
    } catch (AuthCache.PermissionCacheException e) {
      logger.warn("Exception loading user cache for {}, query source auth system", getUserId());
    }
      
    logger.debug("Loading user details for token {}", getAccessToken());
    UserInfo user = authenticationInstance.getUser(accessToken);

    setAccessToken(accessToken);
    setUserId(user.getUserId());
    
    logger.debug("token ok, loaded user {}", user.getUserId());
    
    return user;
  }

  @Override
  public String getAccessToken() {
    return accessToken;
  }

  @Override
  public String getUserId() {
    return userId;
  }

  @Override
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  @Override
  public void setUserId(String userId) {
    this.userId = userId;
  }
  
  public static void main(String[] argv) throws AuthorizationException, AutenticationException {
    
    Map<String, Object> config = new HashMap();
    
    config.put("type", "token");
    config.put("token_url", "http://raptorbox.eu/api/token/check");
    config.put("cache_type", "memory");
    
    RaptorAuth auth = new RaptorAuth();
    auth.initialize(config);
    
    UserInfo user = auth.getUser("Bearer TEST");
    
    System.out.println("UserInfo: " +user.toString() );
    
    auth.isAuthorized("myObjectId", Permission.Read);
    
  }
  
}
