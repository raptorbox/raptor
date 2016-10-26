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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.logging.Level;
import org.createnet.raptor.auth.AuthConfiguration;
import org.createnet.raptor.auth.AuthHttpClient;
import org.createnet.raptor.auth.authentication.AbstractAuthentication;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.entity.SyncRequest;
import org.createnet.raptor.auth.entity.AuthorizationRequest;
import org.createnet.raptor.auth.entity.AuthorizationResponse;
import org.createnet.raptor.auth.entity.LoginRequest;
import org.createnet.raptor.auth.entity.LoginResponse;
import org.createnet.raptor.models.objects.ServiceObject;
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
    public Authentication.UserInfo getUser(String accessToken) {

        try {

            logger.debug("Loading user by token {}", accessToken);

            AuthorizationResponse response = getUserRequest(accessToken);

            if (response.userId == null) {
                throw new AuthenticationException("User id not found in response");
            }

            return new Authentication.UserInfo(response.userId, accessToken, response.details);

        } catch (AuthHttpClient.ClientException ex) {
            logger.debug("Failed to load user: {} ({})", ex.getReason(), ex.getCode());
            throw new AuthenticationException(ex);
        }
    }

    @Override
    public UserInfo login(String username, String password) throws AuthenticationException {

        try {
            
            logger.debug("Loading user by login {}", username);

            LoginResponse response = loginUserRequest(username, password);

            if (response.token == null) {
                throw new AuthenticationException("Token not provided in the response");
            }

            return getUser(response.token);

        } catch (AuthHttpClient.ClientException ex) {
            logger.debug("Failed to login user: {} ({})", ex.getReason(), ex.getCode());
            throw new AuthenticationException(ex);
        }        
    }

    @Override
    public void initialize(AuthConfiguration configuration) {
        super.initialize(configuration);
        client.setConfig(configuration);
    }

    @Override
    public void sync(String accessToken, ServiceObject obj, SyncOperation op) {
        try {
            logger.debug("Syncing object op:{} for id:{}", op.name(), obj.id);

            SyncRequest synreq = new SyncRequest();
            synreq.userId = obj.getUserId();
            synreq.objectId = obj.getId();
            synreq.created = obj.createdAt;
            synreq.operation = op.name().toLowerCase();

            String payload = mapper.writeValueAsString(synreq);

            client.sync(accessToken, payload);

        } catch (AuthHttpClient.ClientException | JsonProcessingException ex) {
            throw new AuthenticationException(ex);
        }
    }

    protected AuthorizationResponse getUserRequest(String accessToken) {

        try {
            AuthorizationRequest areq = new AuthorizationRequest(AuthorizationRequest.Operation.User);

            String payload = mapper.writeValueAsString(areq);
            String response = client.check(accessToken, payload);
            return mapper.readValue(response, AuthorizationResponse.class);
        } catch (IOException ex) {
            throw new AuthenticationException(ex);
        }
    }

    protected LoginResponse loginUserRequest(String username, String password) {

        try {
            
            LoginRequest lreq = new LoginRequest(username, password);

            String payload = mapper.writeValueAsString(lreq);
            String response = client.login(payload);
            
            return mapper.readValue(response, LoginResponse.class);
        } catch (IOException ex) {
            throw new AuthenticationException(ex);
        }
    }

}
