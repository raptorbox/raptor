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
import org.createnet.raptor.models.auth.StaticGroup;
import org.createnet.raptor.models.auth.User;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class AppUser {

    protected String uuid;
    protected List<String> groups = new ArrayList();

    public AppUser() {
    }
    
    public AppUser(String userId) {
        this.uuid = userId;
    }
    
    public AppUser(User user) {
        this(user.getId());
    }
    
    public String getId() {
        return uuid;
    }

    public void setId(String id) {
        this.uuid = id;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public void addGroups(List<String> groups) {
        groups.forEach((group) -> {
            if (!getGroups().contains(group)) {
                getGroups().add(group);
            }
        });
    }

    public void removeGroup(AppGroup group) {
        if (getGroups().contains(group.getName())) {
            getGroups().remove(group.getName());
        }
    }

    public void addGroup(AppGroup group) {
        addGroups(Arrays.asList(group.getName()));
    }
    
    public boolean hasGroup(StaticGroup group) {
        return hasGroup(group.name());
    }
    
    public boolean hasGroup(String group) {
        return getGroups().contains(group);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.uuid);
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
        if (!Objects.equals(this.uuid, other.uuid)) {
            return false;
        }
        return true;
    }
    
}
