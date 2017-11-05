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
package org.createnet.raptor.models.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.createnet.raptor.models.auth.StaticGroup;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class AuthConfiguration {
    
    private final String serviceUserType = "service";
    
    private List<AdminUser> users = new ArrayList();
    private String header = "authorization";
    private String headerPrefix = "Bearer ";
    private String defaultToken = "service-default";
    private String secret;
    private int expiration = 1800;

    public boolean userHasLock(final String username) {
        return getUsers().stream().filter(u -> u.getUsername() != null && u.getUsername().equals(username)).filter(u -> u.isLocked()).count() == 1;
    }
    
    public AdminUser getServiceUser() {
        Optional<AdminUser> user = getUsers().stream().filter(u -> u.getType() != null && u.getType().equals(serviceUserType)).findFirst();
        return user.isPresent() ? user.get() : null;
    }
    
    public static class AdminUser {

        private String username;
        private String password;
        private String token;
        private String email;
        private String type;
        private boolean lock;
        private List<StaticGroup> groups = new ArrayList();
        
        public boolean isAdmin() {
            return getRoles().contains(StaticGroup.admin);
        }
        
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
        
        public boolean isLocked() {
            return lock;
        }
        
        public boolean getLock() {
            return lock;
        }

        public void setLock(boolean lock) {
            this.lock = lock;
        }
        
        public List<StaticGroup> getRoles() {
            return groups;
        }

        public void setRoles(List<StaticGroup> groups) {
            this.groups = groups;
        }
        
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
        
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getHeaderPrefix() {
        return headerPrefix;
    }

    public void setHeaderPrefix(String headerPrefix) {
        this.headerPrefix = headerPrefix;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getExpiration() {
        return expiration;
    }

    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    public List<AdminUser> getUsers() {
        return users;
    }

    public void setUsers(List<AdminUser> users) {
        this.users = users;
    }

    public String getDefaultToken() {
        return defaultToken;
    }

    public void setDefaultToken(String defaultToken) {
        this.defaultToken = defaultToken;
    }
    
}
