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
package org.createnet.raptor.client.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.createnet.raptor.client.AbstractClient;
import org.createnet.raptor.client.Raptor;
import org.createnet.raptor.client.api.HttpClient;
import org.createnet.raptor.models.auth.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods to interact with Raptor API
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class UserClient extends AbstractClient {

    private class JsonUser extends User {

        public JsonUser(User user) {
            super(user);
        }

        public JsonUser() {
        }

        @JsonProperty(access = JsonProperty.Access.READ_WRITE)
        protected String password;
        
    }

    public UserClient(Raptor container) {
        super(container);
    }

    final static Logger logger = LoggerFactory.getLogger(UserClient.class);

    /**
     * Get an user
     *
     * @param userUuid
     * @return
     */
    public User get(String userUuid) {
        JsonNode node = getClient().get(String.format(HttpClient.Routes.USER_GET, userUuid));
        return getMapper().convertValue(node, User.class);
    }

    /**
     * Create a new user
     *
     * @param user
     * @return
     */
    public User create(User user) {
        JsonUser jsonUser = new JsonUser(user);
        JsonNode node = getClient().post(HttpClient.Routes.USER_CREATE, toJsonNode(jsonUser));
        return getMapper().convertValue(node, User.class);
    }

    /**
     * Update a user
     *
     * @param user
     * @return
     */
    public User update(User user) {
        assert user.getUuid() != null;
        JsonUser jsonUser = new JsonUser(user);
        JsonNode node = getClient().put(String.format(HttpClient.Routes.USER_UPDATE, user.getUuid()), toJsonNode(jsonUser));
        return getMapper().convertValue(node, User.class);
    }

    /**
     * Delete a user
     *
     * @param user
     * @return
     */
    public void delete(User user) {
        assert user.getUuid() != null;
        delete(user.getUuid());
    }

    /**
     * Delete a user
     *
     * @param userUuid
     * @return
     */
    public void delete(String userUuid) {
        getClient().delete(String.format(HttpClient.Routes.USER_DELETE, userUuid));
    }

    /**
     * Create a new user setting the minimum required parameters
     *
     * @param username
     * @param password
     * @param email
     * @return
     */
    public User create(String username, String password, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        return create(user);
    }

}
