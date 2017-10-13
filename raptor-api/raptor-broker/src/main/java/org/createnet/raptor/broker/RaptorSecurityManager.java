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
package org.createnet.raptor.broker;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.security.cert.X509Certificate;
import org.apache.activemq.artemis.core.security.CheckType;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.spi.core.protocol.RemotingConnection;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager2;
import org.createnet.raptor.common.client.InternalApiClientService;
import org.createnet.raptor.models.acl.Permissions;
import org.createnet.raptor.models.auth.request.AuthorizationResponse;
import org.createnet.raptor.models.auth.Role.Roles;
import org.createnet.raptor.models.configuration.AuthConfiguration;
import org.createnet.raptor.models.configuration.RaptorConfiguration;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.Topics;
import org.createnet.raptor.sdk.api.AuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Component
public class RaptorSecurityManager implements ActiveMQSecurityManager2 {

    @Autowired
    RaptorConfiguration config;

    @Autowired
    InternalApiClientService apiClient;

    private final Logger logger = LoggerFactory.getLogger(RaptorSecurityManager.class);

    protected BrokerUser login(String token) {
        try {

            Raptor r = new Raptor(config.getUrl(), token);
            AuthClient.LoginState result = r.Auth().login();
            logger.debug("Authenticated user {}", result.user.getUuid());

            return new BrokerUser(r);

        } catch (Exception e) {
            logger.error("Token authentication failed ({})", e.getMessage());
            logger.debug("Faulty token={}", token);
        }
        return null;
    }

    protected BrokerUser getLocalUser(final String username, final String password) {

        List<AuthConfiguration.AdminUser> users = config.getAuth().getUsers().stream().filter((AuthConfiguration.AdminUser user) -> {

            if (user.getPassword() == null || user.getPassword().isEmpty() || password == null || password.isEmpty()) {
                return false;
            }

            return user.getPassword().equals(password);

        }).collect(Collectors.toList());

        if (users.isEmpty()) {
            return null;
        }

        return new BrokerUser(users.get(0));
    }

    protected BrokerUser login(String username, String password) {
        try {

            Raptor r = new Raptor(config.getUrl(), username, password);
            AuthClient.LoginState result = r.Auth().login();
            logger.debug("Login successful for user {}", result.user.getUuid());

            return new BrokerUser(r);
        } catch (Exception e) {
            logger.error("Credential login failed [user={}]", username);
        }

        return null;
    }

    protected BrokerUser authenticate(String username, String password) {

        BrokerUser brokerUser = null;

        // 1. if no username, try with apiKey authentication
        if (username == null || username.isEmpty() || username.length() <= 3) {
            if (password == null || password.isEmpty()) {
//                logger.debug("Attempt to login with empty token! [user={}]", username);
                return null;
            }
            logger.debug("Trying token login");
            brokerUser = login(password);
        } else {

            // 2. try to login from local configuration file
            BrokerUser localUser = getLocalUser(username, password);
            logger.debug("Try local login for user {}", username);
            if (localUser != null) {
                logger.debug("Local user {} found", username);
                brokerUser = localUser;
            }

            if (brokerUser == null) {
                // 3. try to login as user to the auth api
                logger.debug("Authenticate on auth API user {}", username);
                brokerUser = login(username, password);
            }
        }

        return brokerUser;
    }

    @Override
    public boolean validateUser(String user, String password, X509Certificate[] certificates) {
        boolean isAuthenticated = authenticate(user, password) != null;
        return isAuthenticated;
    }

    @Override
    public boolean validateUserAndRole(String username, String password, Set<Role> roles, CheckType checkType, String address, RemotingConnection connection) {

        logger.debug("Authenticating user {} with roles {} on topic {}", username, roles, address);

        BrokerUser brokerUser = authenticate(username, password);

        if (brokerUser == null) {
            logger.debug("Login failed for `{}`", username);
            return false;
        }

        boolean isLocalAdmin = brokerUser.isLocal() && brokerUser.isAdmin();
        if (isLocalAdmin) {
            return true;
        }

        boolean isMqttTopic = (address.contains("$sys.mqtt.queue.qos")
                || address.contains("$sys.mqtt.#"));
        boolean isJmsQueue = (address.contains("jms.queue"));

        if (isMqttTopic || isJmsQueue) {

            switch (checkType) {

                case CREATE_DURABLE_QUEUE:
                case DELETE_DURABLE_QUEUE:

                case CREATE_NON_DURABLE_QUEUE:
                case DELETE_NON_DURABLE_QUEUE:

                case CONSUME:
                case SEND:

                    return true;
                case MANAGE:
                    if (isLocalAdmin) {
                        return true;
                    }
            }

            return false;
        }

        logger.debug("Validating topic {}", address);

        if (address.substring(0, 1).equals(".")) {
            address = address.substring(1);
        }

        String[] topicTokens = address.split("\\.");
        if (topicTokens.length >= 2) {

            Raptor r = brokerUser.getRaptor();

            if (r == null) {
                logger.warn("Raptor client not available [local user: {}]", brokerUser.isLocal());
                return false;
            }

            // eg. device.uuid
            try {

                String type = topicTokens[0];
                String id = topicTokens[1];

                switch (Topics.Types.valueOf(type)) {
                    case device:
                        return hasDevicePermission(r, id, Permissions.admin);
                    case action:
                        return hasDevicePermission(r, id, Permissions.execute);
                    case stream:
                        return hasDevicePermission(r, id, Permissions.pull);
                    case tree:
                        return hasDevicePermission(r, null, Permissions.tree);
                    case token:
                    case user:
                        return r.Auth().getUser().isSuperAdmin();
                }

                logger.error("Unrecognized subscribe topic pattern {}", address);

                return false;

            } catch (Exception ex) {
                logger.error("Failed to subscribe: {}", ex.getMessage(), ex);
                return false;
            }
        }

        return false;
    }

    protected boolean hasDevicePermission(Raptor r, String id, Permissions perm) {
        AuthorizationResponse req = apiClient.Admin().User().isAuthorized(id, r.Auth().getUser().getUuid(), perm);
        return req.result;
    }

    @Override
    public boolean validateUser(String user, String password) {
        logger.debug("Authenticate user {} with token {}", user, password);
        return validateUser(user, password, null);
    }

    @Override
    public boolean validateUserAndRole(String user, String password, Set<Role> roles, CheckType checkType) {
        logger.warn("validateUserAndRole(user, password, roles, checkType): NOT IMPLEMENTED");
        return roles.contains(Roles.admin.name()) && validateUser(user, password);
    }

}
