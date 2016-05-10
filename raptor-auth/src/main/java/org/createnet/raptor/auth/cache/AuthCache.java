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
package org.createnet.raptor.auth.cache;

import java.util.Map;
import org.createnet.raptor.auth.AuthConfiguration;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authorization.Authorization;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public interface AuthCache {

  public class PermissionCacheException extends Exception {

    public PermissionCacheException(Throwable t) {
      super(t);
    }

    public PermissionCacheException(String m, Throwable t) {
      super(m, t);
    }

    public PermissionCacheException(String m) {
      super(m);
    }
  }

  public class UserCacheException extends Exception {

    public UserCacheException(Throwable t) {
      super(t);
    }

    public UserCacheException(String m, Throwable t) {
      super(m, t);
    }
  }

  
  public void initialize(AuthConfiguration configuration);
  public void setup();
  public void clear();
  
  public Boolean get(String userId, String id, Authorization.Permission op) throws PermissionCacheException;
  public void set(String userId, String id, Authorization.Permission op, boolean result) throws PermissionCacheException;
  
  public Authentication.UserInfo get(String accessToken) throws PermissionCacheException;
  public void set(Authentication.UserInfo user) throws PermissionCacheException;
  
}
