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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.createnet.raptor.models.acl.EntityType;
import org.createnet.raptor.models.acl.Operation;
import org.createnet.raptor.models.app.App;
import org.createnet.raptor.models.auth.StaticGroup;
import org.createnet.raptor.models.auth.Role;
import org.createnet.raptor.models.auth.Permission;
import org.createnet.raptor.sdk.AbstractClient;
import org.createnet.raptor.sdk.Raptor;

import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.auth.request.AuthorizationRequest;
import org.createnet.raptor.models.auth.request.AuthorizationResponse;
import org.createnet.raptor.models.auth.request.SyncRequest;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.createnet.raptor.models.payload.UserPayload;
import org.createnet.raptor.models.tree.TreeNode;
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
     * @param operation
     * @return
     */
    public AuthorizationResponse isAuthorized(Device device, User user, Operation operation) {
        return isAuthorized(getContainer().Auth().getUser().getId(), EntityType.device, operation, device.id(), device.getDomain());
    }

    /**
     * Check if the current user is authorized to operate on a device
     *
     * @param device
     * @param operation
     * @return
     */
    public AuthorizationResponse isAuthorized(Device device, Operation operation) {
        return isAuthorized(getContainer().Auth().getUser().getId(), EntityType.device, operation, device.id(), device.getDomain());
    }

    /**
     * Check if an user is authorized to operate on a device
     *
     * @param userId
     * @param type
     * @param operation
     * @param objectId
     * @param domain
     * @return
     */
    public AuthorizationResponse isAuthorized(String userId, EntityType type, Operation operation, String objectId, String domain) {

        AuthorizationRequest auth = new AuthorizationRequest();
        auth.subjectId = objectId;
        auth.permission = operation;
        auth.userId = userId;
        auth.type = type;
        auth.domain = domain;

        return can(auth);
    }

    /**
     * Check if an user is authorized to operate on a device
     *
     * @param userId
     * @param type
     * @param operation
     * @param objectId
     * @return
     */
    public AuthorizationResponse isAuthorized(String userId, EntityType type, Operation operation, String objectId) {
        return isAuthorized(userId, type, operation, objectId, null);
    }
    
    /**
     * Check if an user is authorized to operate on a device
     *
     * @param auth
     * @return
     */
    public AuthorizationResponse can(AuthorizationRequest auth) {
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
     * Register a device in the ACL system
     *
     * @param op
     * @param subj
     */
    public void sync(Operation op, Device subj) {
        sync(EntityType.device, op, subj.getId(), subj.getUserId());
    }
    
    /**
     * Register a node in the ACL system
     *
     * @param op
     * @param subj
     */
    public void sync(Operation op, TreeNode subj) {
        sync(EntityType.tree, op, subj.getId(), subj.getOwnerId());
    }
    
    /**
     * Register an app in the ACL system
     *
     * @param op
     * @param subj
     */
    public void sync(Operation op, App subj) {
        sync(EntityType.app, op, subj.getId(), subj.getOwnerId());
    }
    
    /**
     * Register an entity in the ACL system
     *
     * @param type
     * @param userId
     * @param op
     * @param subjectId
     */
    public void sync(EntityType type, Operation op, String subjectId, String userId) {
        SyncRequest req = new SyncRequest();
        req.subjectId = subjectId;
        req.userId = userId;
        req.permission = op;
        req.type = type;
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
        assert user.getId() != null;
        JsonUser jsonUser = new JsonUser(user);
        JsonNode node = getClient().put(String.format(Routes.USER_UPDATE, user.getId()), toJsonNode(jsonUser));
        return getMapper().convertValue(node, User.class);
    }

    /**
     * Delete a user
     *
     * @param user
     * @return
     */
    public void delete(User user) {
        assert user.getId() != null;
        delete(user.getId());
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
        return create(username, password, email, new ArrayList());
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
        return create(username, password, email, Arrays.asList(new Role(StaticGroup.admin)));
    }

    /**
     * Create a new user setting the minimum required parameters
     *
     * @param username
     * @param password
     * @param email
     * @param groups
     * @return
     */
    public User create(String username, String password, String email, List<Role> groups) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setRoles(groups);
        return create(user);
    }

}
