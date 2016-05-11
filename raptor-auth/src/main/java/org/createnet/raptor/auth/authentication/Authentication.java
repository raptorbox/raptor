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
package org.createnet.raptor.auth.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import org.createnet.raptor.auth.AuthConfiguration;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public interface Authentication {
  
  public class AutenticationException extends Exception {
    public AutenticationException(Throwable t) {
      super(t);
    }
    public AutenticationException(String m, Throwable t) {
      super(m, t);
    }  
    public AutenticationException(String m) {
      super(m);
    }  
  }
  
  public class UserInfo {
    
    final protected List<String> roles = new ArrayList();
    protected String userId;
    protected String accessToken;
    
    protected JsonNode details;
    
    public UserInfo() {
      // set default
      roles.add("USER");
    }
    
    public UserInfo(String userId, String accessToken) {
      this();
      this.userId = userId;
      this.accessToken = accessToken;
    }
    
    public UserInfo(String userId, String accessToken, JsonNode details) {
      this();
      this.userId = userId;
      this.accessToken = accessToken;
      this.details = details;
    }
    
    public String getUserId() {
      return userId;
    }

    public String getAccessToken() {
      return accessToken;
    }

    public JsonNode getDetails() {
      return details;
    }

    @Override
    public String toString() {
      return "[userId = " + getUserId() +
                ", accessToken = " + getAccessToken() +
                ", details = " + getDetails().toString() +
              "]";
    }

    public List<String> getRoles() {
      return roles;
    }
    
    public void setRoles(List<String> newRoles) {
      roles.addAll(newRoles);
    }
    
    public void addRole(String newRole) {
      roles.add(newRole);
    }
    
  }
  
  public void initialize(AuthConfiguration configuration);
  public UserInfo getUser(String accessToken) throws AutenticationException;
  
}
