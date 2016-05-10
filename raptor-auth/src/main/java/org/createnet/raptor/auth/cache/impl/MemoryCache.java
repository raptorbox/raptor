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
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.auth.cache.AbstractCache;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class MemoryCache extends AbstractCache {

  final private int maxSize = 1000;

  private final Map<String, Authentication.UserInfo> users = new HashMap();
  private final Map<String, Boolean> permissions = new HashMap();

  @Override
  public Boolean get(String userId, String id, Authorization.Permission op) throws PermissionCacheException {
    String key = userId + id + op.name();
    return permissions.get(key);
  }

  @Override
  public void set(Authentication.UserInfo user) throws PermissionCacheException {
    
    if (users.size() > maxSize) {
      for (String key : users.keySet()) {
        users.remove(key);
        break;
      }
    }
    
    users.put(user.getAccessToken(), user);
  }

  @Override
  public void set(String userId, String id, Authorization.Permission op, boolean result) throws PermissionCacheException {

    if (permissions.size() > maxSize) {
      for (String key : permissions.keySet()) {
        permissions.remove(key);
        break;
      }
    }

    String key = userId + id + op.name();
    permissions.put(key, result);
  }

  @Override
  public Authentication.UserInfo get(String accessToken) throws PermissionCacheException {
    return users.get(accessToken);
  }

  @Override
  public void clear() {
    permissions.clear();
    users.clear();
  }

  
  
}
