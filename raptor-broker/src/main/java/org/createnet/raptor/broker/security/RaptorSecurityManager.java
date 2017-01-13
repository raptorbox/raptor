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
package org.createnet.raptor.broker.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.security.cert.X509Certificate;
import org.apache.activemq.artemis.core.security.CheckType;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.spi.core.protocol.RemotingConnection;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager2;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.broker.configuration.BrokerConfiguration;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.service.tools.AuthService;
import org.createnet.raptor.service.tools.IndexerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class RaptorSecurityManager implements ActiveMQSecurityManager2 {

    private BrokerConfiguration brokerConfiguration;

    protected enum Roles {
        user, admin
    }

    private final Logger logger = LoggerFactory.getLogger(RaptorSecurityManager.class);

    private final Map<String, BrokerConfiguration.BrokerUser> localUsers = new HashMap();

    @Inject
    AuthService auth;

    @Inject
    IndexerService indexer;

    public RaptorSecurityManager() {

    }

    protected Authentication.UserInfo getUser(String token) {
        try {

            Authentication.UserInfo user = auth.getUser(token);
            if (user != null) {
                logger.debug("Authenticated user {}", user.getUserId());
                return user;
            }

        } catch (ConfigurationException | Authentication.AuthenticationException e) {
            logger.error("Authentication failed");
        }
        return null;
    }

    protected BrokerConfiguration.BrokerUser getLocalUser(String username, String password) {
        BrokerConfiguration.BrokerUser user = localUsers.getOrDefault(username, null);
        return (user != null && user.login(password)) ? user : null;
    }

    protected Authentication.UserInfo login(String username, String password) {
        try {

            Authentication.UserInfo user = auth.login(username, password);
            if (user != null) {
                logger.debug("Login successful for user {}", user.getUserId());
                return user;
            }

        } catch (ConfigurationException | Authentication.AuthenticationException e) {
            logger.error("Login failed");
        }
        return null;
    }

    protected Authentication.UserInfo authenticate(String username, String password) {

        logger.debug("Authenticate user {}", username);

        Authentication.UserInfo user = null;

        // 1. if no username, try with apiKey authentication
        if (username == null || username.isEmpty()) {
            user = getUser(password);
        } else {

            // 2. try to login from local configuration file
            BrokerConfiguration.BrokerUser localUser = getLocalUser(username, password);
            if (localUser != null) {
                logger.debug("Local user {} found", username);
                user = new Authentication.UserInfo(username, password);
                user.setRoles(localUser.getRoles());
            }

            if (user == null) {
                // 3. try to login as user to the auth api
                user = login(username, password);
            }
        }

        if (user == null) {
            logger.debug("Login failed for {}:{}", username, password);
            return null;
        }

        return user;
    }

    @Override
    public boolean validateUser(String user, String password, X509Certificate[] certificates) {
        boolean isAuthenticated = authenticate(user, password) != null;
        return isAuthenticated;
    }

    @Override
    public boolean validateUserAndRole(String username, String password, Set<Role> roles, CheckType checkType, String address, RemotingConnection connection) {

        logger.debug("Authenticate user {} witg roles {} on topic/address {}", username, roles, address);

        Authentication.UserInfo user = authenticate(username, password);

        if (user == null) {
            logger.debug("User `{}` login failed", username);
            return false;
        }

        boolean isAdmin = user.hasRole(Roles.admin.toString());

        if (isAdmin) {
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
                    if (isAdmin) {
                        return true;
                    }
            }

            return false;
        }

        logger.debug("Validating topic {}", address);
        String[] topicTokens = address.split("\\.");
        if (topicTokens.length >= 2) {

            int soidIndex = 0;
            String objectId = topicTokens[soidIndex];

            try {

                ServiceObject obj = indexer.getObject(objectId);
                if (obj == null) {
                    logger.warn("Object not found, id: `{}`", objectId);
                    return false;
                }

                logger.debug("Check access permission of user {} to object {}", user.getUserId(), objectId);

                // @NOTE: this was the subscribe permission but has been migrted to Pull to simplify the flow
                boolean allowed = auth.isAllowed(user.getAccessToken(), obj, Authorization.Permission.Pull);
                return allowed;

            } catch (Authorization.AuthorizationException | Storage.StorageException | RaptorComponent.ParserException | ConfigurationException ex) {
                logger.error("Failed to subscribe: {}", ex.getMessage(), ex);
                return false;
            }
        }

        return false;
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

    public void setBrokerConfiguration(BrokerConfiguration brokerConfiguration) {

        localUsers.clear();
        for (BrokerConfiguration.BrokerUser user : brokerConfiguration.users) {
            localUsers.put(user.name, user);
        }

        this.brokerConfiguration = brokerConfiguration;
    }

}
