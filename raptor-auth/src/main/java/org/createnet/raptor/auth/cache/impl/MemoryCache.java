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
package org.createnet.raptor.auth.cache.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.auth.cache.AbstractCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class MemoryCache extends AbstractCache {

  private final Logger logger = LoggerFactory.getLogger(MemoryCache.class);
  
  final private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);  
  
  static private final Map<String, CachedItem<Authentication.UserInfo>> users = new HashMap();
  static private final Map<String, CachedItem<Boolean>> permissions = new HashMap();

  static protected class CachedItem<T> {
    
    static public int defaultTTL = 10000; // 10 sec TTL
    
    final private T item;
    final private long expiry;

    public CachedItem(T item, long expiry) {
      this.item = item;
      this.expiry = expiry;
    }
    
    public CachedItem(T item) {
      this.item = item;
      this.expiry = System.currentTimeMillis() + defaultTTL;
    }
    
    public boolean isExpired() {
      return expiry < System.currentTimeMillis();
    }

    public T getItem() {      
      return isExpired() ? null : item;
    }
    
  }
  
  public MemoryCache() {
    try {

      scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
          
          for(Map.Entry<String, CachedItem<Authentication.UserInfo>> el : getUsers().entrySet()) {
            if(el.getValue().isExpired()) {
              getUsers().remove(el.getKey());
              logger.debug("Removed cached user {}", el.getKey());
            }
          }

          for(Map.Entry<String, CachedItem<Boolean>> el : getPermissions().entrySet()) {
            if(el.getValue().isExpired()) {
              getPermissions().remove(el.getKey());
              logger.debug("Removed cached permission {}", el.getKey());
            }
          }

        }
      }, 0, 5, TimeUnit.SECONDS);
    }
    catch(RuntimeException ex) {
      logger.warn("Scheduled task exception", ex);
    }
    
  }
  
  @Override
  public Boolean get(String userId, String id, Authorization.Permission op) throws PermissionCacheException {
    if(id == null) id = "";
    String key = userId + id + op.toString();
    CachedItem<Boolean> cache = getPermissions().get(key);
    return cache == null ? null : cache.getItem();
  }

  @Override
  public void set(Authentication.UserInfo user) throws PermissionCacheException {
    getUsers().put(user.getAccessToken(), new CachedItem(user));
  }

  @Override
  public void set(String userId, String id, Authorization.Permission op, boolean result) throws PermissionCacheException {
    String key = userId + id + op.toString();
    getPermissions().put(key, new CachedItem(result));
  }

  @Override
  public Authentication.UserInfo get(String accessToken) throws PermissionCacheException {
    CachedItem<Authentication.UserInfo> cache = getUsers().get(accessToken);
    return cache == null ? null : cache.getItem();
  }

  @Override
  public void clear() {
    getPermissions().clear();
    getUsers().clear();
  }

  synchronized public static Map<String, CachedItem<Authentication.UserInfo>> getUsers() {
    return users;
  }

  synchronized public static Map<String, CachedItem<Boolean>> getPermissions() {
    return permissions;
  }
  
}
