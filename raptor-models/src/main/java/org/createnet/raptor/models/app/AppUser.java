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
package org.createnet.raptor.models.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.createnet.raptor.models.auth.User;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class AppUser {

    protected String id;
    protected boolean enabled;
    protected List<String> roles = new ArrayList();

    public AppUser() {
    }
    
    public AppUser(String userId) {
        this.id = userId;
    }
    
    public AppUser(User user) {
        this(user.getId());
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void addRoles(List<String> roles) {
        roles.forEach((role) -> {
            if (!getRoles().contains(role)) {
                getRoles().add(role);
            }
        });
    }

    public void removeRole(AppRole role) {
        if (getRoles().contains(role.getName())) {
            getRoles().remove(role.getName());
        }
    }

    public void addRole(AppRole role) {
        addRoles(Arrays.asList(role.getName()));
    }
    
    public boolean hasRole(String role) {
        return getRoles().contains(role);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AppUser other = (AppUser) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
}
