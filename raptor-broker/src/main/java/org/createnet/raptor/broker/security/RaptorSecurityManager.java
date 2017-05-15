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
package org.createnet.raptor.broker.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.security.cert.X509Certificate;
import org.apache.activemq.artemis.core.security.CheckType;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.spi.core.protocol.RemotingConnection;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager2;
import org.createnet.raptor.broker.configuration.BrokerConfiguration;
import org.createnet.raptor.models.acl.Permissions;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.auth.request.AuthorizationResponse;
import org.createnet.raptor.models.auth.Role.Roles;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.api.AuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class RaptorSecurityManager implements ActiveMQSecurityManager2 {

    private BrokerConfiguration brokerConfiguration;

    protected class BrokerUser {

        private User user;
        private Raptor raptor;

        public BrokerUser() {
        }
        
        public BrokerUser(Raptor raptor) {
            this.raptor = raptor;
        }

        public BrokerUser(Raptor raptor, User user) {
            this.raptor = raptor;
            this.user = user;
        }
        
        public BrokerUser(User user) {
            this.user = user;
        }

        public User getUser() {
            if (user == null && raptor != null) {
                return raptor.Auth().getUser();
            }
            return user;
        }
        
        public Raptor getRaptor() {
            return raptor;
        }

    }

    private final Logger logger = LoggerFactory.getLogger(RaptorSecurityManager.class);

    private final Map<String, BrokerConfiguration.BrokerUser> localUsers = new HashMap();

    public RaptorSecurityManager() {

    }

    protected BrokerUser login(String token) {
        try {

            Raptor r = new Raptor(brokerConfiguration.authUrl, token);

            AuthClient.LoginState result = r.Auth().login();
            logger.debug("Authenticated user {}", result.user.getUuid());

            return new BrokerUser(r);
        } catch (Exception e) {
            logger.error("Authentication failed");
        }
        return null;
    }

    protected BrokerConfiguration.BrokerUser getLocalUser(String username, String password) {
        BrokerConfiguration.BrokerUser user = localUsers.getOrDefault(username, null);
        return (user != null && user.login(password)) ? user : null;
    }

    protected BrokerUser login(String username, String password) {
        try {

            Raptor r = new Raptor(brokerConfiguration.authUrl, username, password);

            AuthClient.LoginState result = r.Auth().login();
            logger.debug("Login successful for user {}", result.user.getUuid());

            return new BrokerUser(r);
        } catch (Exception e) {
            logger.error("Login failed");
        }
        return null;
    }

    protected BrokerUser authenticate(String username, String password) {

        logger.debug("Authenticate user {}", username);

        BrokerUser brokerUser = null;

        // 1. if no username, try with apiKey authentication
        if (username == null || username.isEmpty() || username.length() < 3) {
            brokerUser = login(password);
        } else {

            // 2. try to login from local configuration file
            BrokerConfiguration.BrokerUser localUser = getLocalUser(username, password);
            if (localUser != null) {
                
                logger.debug("Local user {} found", username);
                
                User user = new User();
                user.setUsername(username);
                user.setPassword(password);
                // local users must be admin or they won't  pass ACL checks
                user.addRole(localUser.getRoles().contains("admin") ? Roles.admin : Roles.guest);
                
                brokerUser = new BrokerUser(user);
            }

            if (brokerUser == null) {
                // 3. try to login as user to the auth api
                brokerUser = login(username, password);
            }
        }

        if (brokerUser == null) {
            logger.debug("Login failed for {}:{}", username, password);
            return null;
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

        logger.debug("Authenticate user {} with roles {} on topic/address {}", username, roles, address);

        BrokerUser brokerUser = authenticate(username, password);
        User user = brokerUser.getUser();

        if (user == null) {
            logger.debug("User `{}` login failed", username);
            return false;
        }

        boolean isAdmin = user.isAdmin();

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

        if (address.substring(0, 1).equals(".")) {
            address = address.substring(1);
        }

        String[] topicTokens = address.split("\\.");
        if (topicTokens.length >= 2) {

            Raptor r = brokerUser.getRaptor();
            
            if(r == null) {
                logger.warn("Raptor client not available (is a non-admin local users?): {}", user.getUsername());
                return false;
            }
            
            int soidIndex = 0;
            String objectId = topicTokens[soidIndex];

            try {

                Device obj = r.Inventory().load(objectId);
                if (obj == null) {
                    logger.warn("Object not found, id: `{}`", objectId);
                    return false;
                }

                AuthorizationResponse req;

                // <object>/events -> requires `admin`
                String subtopic = topicTokens[soidIndex + 1];
                if (subtopic != null && subtopic.equals("events")) {
                    req = r.Admin().User().isAuthorized(obj.getId(), user.getUuid(), Permissions.admin);
                    if (!req.result) {
                        return false;
                    }
                }

                // <object>/streams/<stream> -> requires `pull`
                if (subtopic != null && subtopic.equals("streams")) {
                    req = r.Admin().User().isAuthorized(obj.getId(), user.getUuid(), Permissions.pull);
                    if (!req.result) {
                        return false;
                    }
                }

                // <object>/actions/<action> -> requires `execute`
                if (subtopic != null && subtopic.equals("actions")) {
                    req = r.Admin().User().isAuthorized(obj.getId(), user.getUuid(), Permissions.execute);
                    if (!req.result) {
                        return false;
                    }
                }

                return true;

            } catch (Exception ex) {
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
