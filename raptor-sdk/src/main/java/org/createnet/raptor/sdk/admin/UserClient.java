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
package org.createnet.raptor.sdk.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.createnet.raptor.models.acl.Permissions;
import org.createnet.raptor.models.auth.Role;
import org.createnet.raptor.sdk.AbstractClient;
import org.createnet.raptor.sdk.Raptor;

import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.auth.request.AuthorizationRequest;
import org.createnet.raptor.models.auth.request.AuthorizationResponse;
import org.createnet.raptor.models.auth.request.SyncRequest;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.createnet.raptor.models.payload.UserPayload;
import org.createnet.raptor.sdk.RequestOptions;
import org.createnet.raptor.sdk.Routes;
import org.createnet.raptor.sdk.api.AuthClient;
import org.createnet.raptor.sdk.events.callback.UserCallback;
import org.createnet.raptor.sdk.events.callback.UserEventCallback;
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

    protected ProfileClient Profile;

    public ProfileClient Profile() {
        if (Profile == null) {
            Profile = new ProfileClient(getContainer());
        }
        return Profile;
    }

    public UserClient(Raptor container) {
        super(container);
        Profile = new ProfileClient(container);
    }

    final static Logger logger = LoggerFactory.getLogger(UserClient.class);


    /**
     * Register for user events
     *
     * @param user
     * @param callback The callback to fire on event arrival
     */
    public void subscribe(User user, UserEventCallback callback) {
        getEmitter().subscribe(user, callback);
    }

    /**
     * Subscribe to user related events 
     *
     * @param user
     * @param ev
     */
    public void subscribe(User user, UserCallback ev) {
        getEmitter().subscribe(user, (DispatcherPayload payload) -> {
            switch (payload.getType()) {
                case user:
                    ev.callback(user, (UserPayload) payload);
                    break;
            }
        });
    }
    
    /**
     * Check if an user is authorized to operate on a device
     *
     * @param device
     * @param user
     * @param permission
     * @return
     */
    public AuthorizationResponse isAuthorized(Device device, User user, Permissions permission) {
        return isAuthorized(device.id(), user.getUuid(), permission);
    }

    /**
     * Check if the current user is authorized to operate on a device
     *
     * @param device
     * @param permission
     * @return
     */
    public AuthorizationResponse isAuthorized(Device device, Permissions permission) {
        return isAuthorized(device.id(), getContainer().Auth().getUser().getUuid(), permission);
    }

    /**
     * Check if an user is authorized to operate on a device
     *
     * @param deviceId
     * @param userId
     * @param permission
     * @return
     */
    public AuthorizationResponse isAuthorized(String deviceId, String userId, Permissions permission) {

        AuthorizationRequest auth = new AuthorizationRequest();
        auth.objectId = deviceId;
        auth.permission = permission.name();
        auth.userId = userId;

        JsonNode node = getClient().post(Routes.PERMISSION_CHECK, toJsonNode(auth), RequestOptions.retriable());
        return getMapper().convertValue(node, AuthorizationResponse.class);
    }

    /**
     * Register a device for an user in ACL system
     *
     * @param req
     * @param opts
     */
    public void sync(SyncRequest req, RequestOptions opts) {
        getClient().post(Routes.PERMISSION_SYNC, toJsonNode(req), opts);
    }
    
    /**
     * Register a device for an user in ACL system
     *
     * @param req
     */
    public void sync(SyncRequest req) {
        getClient().post(Routes.PERMISSION_SYNC, toJsonNode(req), RequestOptions.retriable().maxRetries(5).waitFor(100));
    }
    
    /**
     * Register a device for an user in ACL system
     *
     * @param op
     * @param device
     */
    public void sync(Permissions op, Device device) {
        SyncRequest req = new SyncRequest();
        req.objectId = device.id();
        req.userId = device.userId();
        req.created = device.getCreatedAt();
        req.operation = op;
        sync(req);
    }

    /**
     * Get an user
     *
     * @param userUuid
     * @return
     */
    public User get(String userUuid) {
        JsonNode node = getClient().get(String.format(Routes.USER_GET, userUuid));
        return getMapper().convertValue(node, User.class);
    }

    /**
     * Get user by token
     *
     * @return
     */
    public User get() {
        JsonNode node = getClient().get(Routes.USER_GET_ME);
        return getMapper().convertValue(node, User.class);
    }

    /**
     * Impersonate an user retrieving a valid login token
     *
     * @param userId User ID to impersonate
     * @return
     */
    public AuthClient.LoginState impersonate(String userId) {
        JsonNode node = getClient().get(String.format(Routes.USER_IMPERSONATE, userId));
        return getMapper().convertValue(node, AuthClient.LoginState.class);
    }

    /**
     * Create a new user
     *
     * @param user
     * @return
     */
    public User create(User user) {
        JsonUser jsonUser = new JsonUser(user);
        JsonNode node = getClient().post(Routes.USER_CREATE, toJsonNode(jsonUser));
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
        JsonNode node = getClient().put(String.format(Routes.USER_UPDATE, user.getUuid()), toJsonNode(jsonUser));
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
     */
    public void delete(String userUuid) {
        getClient().delete(String.format(Routes.USER_DELETE, userUuid));
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
        return create(username, password, email, new HashSet(Arrays.asList(new Role(Role.Roles.user))));
    }

    /**
     * Create a new admin user setting the minimum required parameters
     *
     * @param username
     * @param password
     * @param email
     * @return
     */
    public User createAdmin(String username, String password, String email) {
        return create(username, password, email, new HashSet(Arrays.asList(new Role(Role.Roles.admin))));
    }

    /**
     * Create a new user setting the minimum required parameters
     *
     * @param username
     * @param password
     * @param email
     * @param roles
     * @return
     */
    public User create(String username, String password, String email, Set<Role> roles) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setRoles(roles);
        return create(user);
    }

}
