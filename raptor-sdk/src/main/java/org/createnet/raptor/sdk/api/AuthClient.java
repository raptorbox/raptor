/*
 * Copyright 2017 FBK/CREATE-NET
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
package org.createnet.raptor.sdk.api;

import org.createnet.raptor.sdk.Routes;
import org.createnet.raptor.sdk.AbstractClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.exception.RequestException;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.sdk.RequestOptions;
import org.createnet.raptor.sdk.exception.AuthenticationFailedException;
import org.createnet.raptor.sdk.exception.MissingAuthenticationException;

/**
 * Represent a Device data stream
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class AuthClient extends AbstractClient {

    protected final String MSG_LOGIN_ERROR = "Authentication failed, please check your credentials";

    protected LoginState state;

    public AuthClient(Raptor container) {
        super(container);
    }

    public class AuthClientException extends RuntimeException {

        public AuthClientException(String message) {
            super(message);
        }

        public AuthClientException(Throwable cause) {
            super(cause);
        }

    }

    static public class LoginCredentialsBody {

        final public String username;
        final public String password;

        public LoginCredentialsBody(String username, String password) {
            this.username = username;
            this.password = password;
        }

    }

    static public class LoginState {
        public Long expires;
        public String token;
        public User user;
    }

    /**
     * Return the current token
     *
     * @return
     */
    public String getToken() {
        if (!getConfig().hasCredentials()) {
            return getConfig().getToken();
        }
        if (state == null) {
            return null;
        }
        return state.token;
    }

    /**
     * Return the current user
     *
     * @return
     */
    public User getUser() {
        if (state == null) {
            return null;
        }
        return state.user;
    }


    /**
     * Login with username and password
     *
     * @param username
     * @param password
     *
     * @return request response
     */
    public LoginState login(String username, String password) {
        return login(username, password, null);
    }
    
    /**
     * Login with username and password
     *
     * @param username
     * @param password
     * @param options
     *
     * @return request response
     */
    public LoginState login(String username, String password, RequestOptions options) {
        try {
            
            JsonNode cred = Device.getMapper().valueToTree(new LoginCredentialsBody(username, password));
            JsonNode node = getClient().post(Routes.LOGIN, cred, options);

            return Device.getMapper().convertValue(node, LoginState.class);
        } catch (RequestException ex) {

            if (ex.getStatus() == 401) {
                throw new AuthenticationFailedException(MSG_LOGIN_ERROR, ex);
            }

            throw ex;
        }
    }

    /**
     * Login with username and password from provided configuration
     *
     * @return
     */
    public LoginState login() {
        return login(null);
    }
    
    /**
     * Login with username and password from provided configuration
     *
     * @param options
     * @return
     */
    public LoginState login(RequestOptions options) {
        
        if (options == null) {
            options = RequestOptions.defaults();
        }
        
        LoginState body;
        if (getConfig().hasCredentials()) {
            body = login(getConfig().getUsername(), getConfig().getPassword(), options.withAuthToken(false));
        } else if (getConfig().hasToken()) {
            try {
                User user = getContainer().Admin().User().get();
                body = new LoginState();
                body.user = user;
            } catch (RequestException ex) {
                if (ex.getStatus() == 401) {
                    throw new AuthenticationFailedException(MSG_LOGIN_ERROR, ex);
                }
                throw ex;
            }
        } else {
            throw new MissingAuthenticationException("At least one of credentials or token is required to login");
        }
        state = body;
        return body;
    }

    /**
     * Generate a new token with extend expiration time
     *
     * @return
     */
    public LoginState refreshToken() {
        JsonNode node = getClient().get(Routes.REFRESH_TOKEN);
        LoginState body = Device.getMapper().convertValue(node, LoginState.class);
        state = body;
        return body;
    }

    /**
     * Logout the user revoking the token
     */
    public void logout() {
        getClient().delete(Routes.LOGOUT);
        state = null;
    }
}
