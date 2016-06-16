/*
 * Copyright 2016 CREATE-NET
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
package org.createnet.raptor.auth.authorization;

import org.createnet.raptor.auth.AuthConfiguration;
import org.createnet.raptor.config.Configuration;
import org.createnet.raptor.plugin.Plugin;

/**
 *
 * @author Luca Capra <luca.capra@create-net.org>
 */
public interface Authorization extends Plugin<AuthConfiguration> {

  public class AuthorizationException extends Exception {

    public AuthorizationException(Throwable t) {
      super(t);
    }

    public AuthorizationException(String m, Throwable t) {
      super(m, t);
    }
  }

  public enum Permission {
    
    List,
    // ServiceObject definition
    Read, Create, Update, Delete,
    // Stream data
    Pull,
    Push,
    Subscribe,
    // Actuation
    Execute

  }

  public boolean isAuthorized(String accessToken, String id, Permission op) throws AuthorizationException;

}
