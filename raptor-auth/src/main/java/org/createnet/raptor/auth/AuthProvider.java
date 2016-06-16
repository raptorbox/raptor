/*
 * Copyright 2016 CREATE-NET http://create-net.org
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.auth.cache.AuthCache;
import org.createnet.raptor.plugin.PluginConfiguration;
import org.createnet.raptor.plugin.PluginLoader;
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
  protected final PluginLoader<AuthCache> cachePluginLoader = new PluginLoader();
  protected final PluginLoader<Authorization> authorizationPluginLoader = new PluginLoader();
  protected final PluginLoader<Authentication> authenticationPluginLoader = new PluginLoader();

  final public static ObjectMapper mapper = new ObjectMapper();
  
  @Override
  public void initialize(AuthConfiguration configuration) {

    this.configuration = configuration;
    
    String cacheType = this.configuration.cache;
    cache = cachePluginLoader.load(cacheType, AuthCache.class);
    cache.setup();

    String authType = this.configuration.type;
    authenticationInstance = authenticationPluginLoader.load(authType, Authentication.class);
    authorizationInstance = authorizationPluginLoader.load(authType, Authorization.class);

  }

  @Override
  public boolean isAuthorized(String accessToken, String id, Permission op) throws AuthorizationException {
    
    try {
    
      UserInfo user = getUser(accessToken);
      
      Boolean cachedValue = cache.get(user.getUserId(), id, op);
      if(cachedValue != null) {
        logger.debug("Reusing permission cache for userId {} objectId {} permission {} = {}", user.getUserId(), id, op.toString(), cachedValue);
        return cachedValue;
      }
      
      logger.debug("Requesting {} permission for object {}", op, id);
      
      boolean isauthorized = authorizationInstance.isAuthorized(accessToken, id, op);
      
      cache.set(user.getUserId(), id, op, isauthorized);
      
      logger.debug("Permission check for user {} object {} permission {} = {}", user.getUserId(), id, op.toString(), isauthorized ? "yes" : "no");
      
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
  public void sync(String accessToken, String id) throws AuthenticationException {
    authenticationInstance.sync(accessToken, id);
  }

  @Override
  public PluginConfiguration getPluginConfiguration() {
    return null;
  }

  @Override
  public AuthConfiguration getConfiguration() {
    return null;
  }
  
}
