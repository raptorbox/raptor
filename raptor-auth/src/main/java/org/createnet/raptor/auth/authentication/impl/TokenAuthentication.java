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
package org.createnet.raptor.auth.authentication.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.createnet.raptor.auth.AuthConfiguration;
import org.createnet.raptor.auth.AuthHttpClient;
import org.createnet.raptor.auth.authentication.AbstractAuthentication;
import org.createnet.raptor.auth.authentication.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class TokenAuthentication extends AbstractAuthentication {
  
  final private Logger logger = LoggerFactory.getLogger(TokenAuthentication.class);
  
  ObjectMapper mapper = new ObjectMapper();
  final private AuthHttpClient client = new AuthHttpClient();

  @Override
  public Authentication.UserInfo getUser(String accessToken) throws AuthenticationException {

    try {
      
      logger.debug("Loading user by token {}", accessToken);
      
      String response = checkRequest(accessToken);
      JsonNode node = mapper.readTree(response);
      
      if(!node.has("id")) {
        throw new AuthenticationException("User id not found in response");
      }
      
      return new Authentication.UserInfo(node.get("userid").asText(), accessToken, node);
      
    } catch (IOException ex) {
      throw new AuthenticationException(ex);
    } catch (AuthHttpClient.ClientException ex) {
      logger.debug("Failed to load user: {} ({})", ex.getReason(), ex.getCode());
      throw new AuthenticationException(ex);
    }
  }

  @Override
  public void initialize(AuthConfiguration configuration) {
    super.initialize(configuration);
    client.setConfig(configuration);
  }

  @Override
  public void sync(String accessToken, String objId) throws AuthenticationException {
    try {
      logger.debug("Syncing object operation for {}", objId);
      client.sync(accessToken, "{\"id\": \""+ objId +"\"}");
    } catch (AuthHttpClient.ClientException ex) {
      throw new AuthenticationException(ex);
    }
  }

  
  protected String checkRequest(String accessToken) throws IOException, AuthHttpClient.ClientException {
    
    List<NameValuePair> args = new ArrayList();
    args.add(new BasicNameValuePair("operation", "loadUser"));
    
    return client.check(accessToken, args);
  }
 
  
}
