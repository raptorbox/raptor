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
package org.createnet.raptor.broker.configuration;

import java.util.ArrayList;
import java.util.List;
import org.createnet.raptor.config.Configuration;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class BrokerConfiguration implements Configuration {

  public static class BrokerUser {

    public String name;
    public String password;
    public List<String> roles = new ArrayList();

    public BrokerUser() {
    }

    public BrokerUser(String name, String password) {
      this.name = name;
      this.password = password;
    }
    
    public boolean login(String password) {
      return this.password.equals(password);
    }
    
    public void addRole(String role) {
      if(!roles.contains(role))
        roles.add(role);
    }
    
    public boolean hasRole(String role) {
      return roles.contains(role);
    }

    public List<String> getRoles() {
      return roles;
    }

  }

  public String artemisConfiguration;
  public List<BrokerUser> users = new ArrayList();

}
