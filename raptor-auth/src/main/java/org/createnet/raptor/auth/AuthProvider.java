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
import java.util.Iterator;
import java.util.ServiceLoader;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.auth.cache.AuthCache;
import org.createnet.raptor.config.Configuration;
import org.createnet.raptor.plugin.Plugin;
import org.createnet.raptor.plugin.PluginConfiguration;
import org.createnet.raptor.plugin.PluginConfigurationLoader;
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
  protected final PluginConfigurationLoader pluginConfigLoader = PluginConfigurationLoader.getInstance();

  final public static ObjectMapper mapper = new ObjectMapper();
  
  @Override
  public void initialize(AuthConfiguration configuration) {

    this.configuration = (AuthConfiguration)configuration;
    
    String cacheType = this.configuration.cache;
    cache = loadCachePlugin(cacheType);
    cache.setup();

    String authType = this.configuration.type;
    authenticationInstance = loadAuthenticationPlugin(authType);
    authorizationInstance = loadAuthorizationPlugin(authType);

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
  
  protected void initializePlugin(Plugin<AuthConfiguration> plugin) {
    
    Configuration config = pluginConfigLoader.load(plugin);
    
    if (config == null) {
      plugin.initialize(null);
      return;
    }
    
    plugin.initialize(plugin.getPluginConfiguration().getType().cast(config));
  }
  
  protected AuthCache loadCachePlugin(String name) {

    Iterator<AuthCache> services = ServiceLoader.load(AuthCache.class).iterator();

    while (services.hasNext()) {
      AuthCache service = services.next();
      if (service.getPluginConfiguration() != null
              && service.getPluginConfiguration().getName().equals(name)) {
        logger.debug("Loaded AuthCache plugin: {}", service.getClass().getName());        
        initializePlugin(service);
        return service;
      }
    }

    throw new RuntimeException("Cannot load AuthCache plugin " + name);
  }
  
  protected Authorization loadAuthorizationPlugin(String name) {

    Iterator<Authorization> services = ServiceLoader.load(Authorization.class).iterator();

    while (services.hasNext()) {
      Authorization service = services.next();
      if (service.getPluginConfiguration() != null
              && service.getPluginConfiguration().getName().equals(name)) {

        logger.debug("Loaded Authorization plugin: {}", service.getClass().getName());        
        initializePlugin(service);
        return service;
      }
    }

    throw new RuntimeException("Cannot load Authorization plugin " + name);
  }

  protected Authentication loadAuthenticationPlugin(String name) {

    Iterator<Authentication> services = ServiceLoader.load(Authentication.class).iterator();

    while (services.hasNext()) {
      Authentication service = services.next();
      if (service.getPluginConfiguration() != null
              && service.getPluginConfiguration().getName().equals(name)) {
        logger.debug("Loaded Authentication plugin: {}", service.getClass().getName());
        initializePlugin(service);
        return service;
      }
    }

    throw new RuntimeException("Cannot load Authentication plugin " + name);
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
