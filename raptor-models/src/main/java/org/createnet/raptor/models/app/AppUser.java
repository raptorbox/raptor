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
import org.createnet.raptor.models.auth.StaticGroup;
import org.createnet.raptor.models.auth.User;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class AppUser {

    protected String id;
    protected List<AppGroup> groups = new ArrayList();

    public AppUser() {
    }
    
    public AppUser(String userId) {
        this.id = userId;
    }
    
    public AppUser(User user) {
        this(user.getUuid());
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<AppGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<AppGroup> groups) {
        this.groups = groups;
    }

    public void addGroups(List<AppGroup> groups) {
        groups.forEach((group) -> {
            if (!getGroups().contains(group)) {
                getGroups().add(group);
            }
        });
    }

    public void removeGroup(AppGroup group) {
        if (getGroups().contains(group)) {
            getGroups().remove(group);
        }
    }

    public void addGroup(AppGroup group) {
        addGroups(Arrays.asList(group));
    }
    
    public boolean hasGroup(StaticGroup group) {
        return getGroups().contains(new AppGroup(group.name()));
    }

}
