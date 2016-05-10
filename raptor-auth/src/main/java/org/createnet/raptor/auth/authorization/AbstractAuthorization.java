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

import java.util.Map;
import org.createnet.raptor.auth.AuthConfiguration;

/**
 *
 * @author Luca Capra <luca.capra@create-net.org>
 */
abstract public class AbstractAuthorization implements Authorization {

  protected String accessToken;
  protected String userId;
  protected AuthConfiguration configuration;
  
  @Override
  public String getAccessToken() {
    return accessToken;
  }
  
  @Override
  public String getUserId() {
    return userId;
  }
  
  @Override
  public void initialize(AuthConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  @Override
  public void setUserId(String userId) {
    this.userId = userId;
  }
  
}
