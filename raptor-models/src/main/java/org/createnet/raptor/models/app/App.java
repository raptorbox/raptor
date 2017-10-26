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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.createnet.raptor.models.acl.Owneable;
import org.createnet.raptor.models.auth.DefaultGroup;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@Document
public class App implements Serializable, Owneable {

    static final long serialVersionUID = 4441L;
    
    @Id
    protected String id = UUID.randomUUID().toString();

    protected String userId;
    protected String name;
    protected String description;

    final protected List<AppGroup> groups = new ArrayList();
    final protected List<String> devices = new ArrayList();
    final protected List<AppUser> users = new ArrayList();

    public App() {
    }

    public App(String name) {
        this.name = name;
    }

    public App(String name, String userId) {
        this(name);
        this.userId = userId;
    }

    public App(String name, User owner) {
        this(name, owner.getUuid());
    }

    public void merge(App raw) {

        if (raw.getName() != null && !raw.getName().isEmpty()) {
            setName(raw.getName());
        }
        if (raw.getDescription() != null && !raw.getDescription().isEmpty()) {
            setDescription(raw.getDescription());
        }
        if (raw.getUserId() != null && !raw.getUserId().isEmpty()) {
            setUserId(raw.getUserId());
        }
        if (raw.getGroups() != null && !raw.getGroups().isEmpty()) {
            raw.getGroups().forEach((r) -> {
                if (!getGroups().contains(r)) {
                    getGroups().add(r);
                }
            });
        }
        if (raw.getUsers() != null && !raw.getUsers().isEmpty()) {
            raw.getUsers().forEach((u) -> {
                if (!getUsers().contains(u)) {
                    getUsers().add(u);
                }
            });
        }
        if (raw.getDevices() != null && !raw.getDevices().isEmpty()) {
            raw.getDevices().forEach((u) -> {
                if (!getDevices().contains(u)) {
                    getDevices().add(u);
                }
            });
        }

    }

    public void validate() {

        if (getId() == null || getId().isEmpty()) {
            throw new RaptorComponent.ValidationException("Id is missing");
        }

        if (getName() == null || getName().isEmpty()) {
            throw new RaptorComponent.ValidationException("Name is missing");
        }

        if (getUserId() == null || getUserId().isEmpty()) {
            throw new RaptorComponent.ValidationException("UserId is missing");
        }

        getUsers().forEach((u) -> {
            u.getGroups().forEach((r) -> {
                if (!getGroups().contains(r)) {
                    throw new RaptorComponent.ValidationException(String.format("User `%s` has an unknown group `%s`", u.getId(), r.getName()));
                }
            });

        });
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AppUser> getUsers() {
        return users;
    }

    public void setUsers(List<AppUser> users) {
        this.users.clear();
        this.users.addAll(users);
    }

    public List<String> getDevices() {
        return devices;
    }

    public void setDevices(List<String> devices) {
        this.devices.clear();
        this.devices.addAll(devices);
    }

    public List<AppGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<AppGroup> groups) {
        this.groups.clear();
        this.groups.addAll(groups);
    }

    public void setOwner(User user) {
        this.userId = user.getUuid();
    }

    public void addUser(User user, List<AppGroup> groups) {

        AppUser appUser = new AppUser();
        appUser.setId(user.getUuid());
        appUser.addGroups(groups);

        this.getUsers().add(appUser);
    }

    public void addGroups(List<AppGroup> groups) {
        groups.forEach((r) -> {
            if (getGroups().contains(r)) {
                getGroups().remove(r);
            }
            getGroups().add(r);
        });
    }

    public void addGroup(AppGroup r) {
        addGroups(Arrays.asList(r));
    }

    public void addGroup(String r, List<String> permissions) {
        addGroup(new AppGroup(name, permissions));
    }

    public void removeGroup(String group) {
        getGroups().forEach((r) -> {
            if (r.getName().equals(group)) {
                getGroups().remove(r);
            }
        });
    }

    public void addDevices(List<Device> devices) {
        devices.forEach((d) -> {
            if (!getDevices().contains(d.getId())) {
                getDevices().add(d.getId());
            }
        });
    }

    public void addDevice(Device d) {
        addDevices(Arrays.asList(d));
    }

    public void addDevice(String d) {
        addDevice(new Device(d));
    }

    public void removeDevice(String d) {
        if (getDevices().contains(d)) {
            getDevices().remove(d);
        }
    }

    public AppGroup getGroup(DefaultGroup searchedGroup) {
        Optional<AppGroup> group = getGroups().stream().filter((r) -> {
            return r.getName().equals(searchedGroup.name());
        }).findFirst();
        return group.isPresent() ? group.get() : null;
    }

    @JsonIgnore
    public AppGroup getAdminGroup() {
        return getGroup(DefaultGroup.admin);
    }

    @JsonIgnore
    public Object getUserGroup() {
        return getGroup(DefaultGroup.user);
    }
    
    public boolean hasGroup(User user, DefaultGroup group) {
        return getUsers().stream().filter((u) -> {
            return u.getId().equals(user.getUuid()) && u.hasGroup(group);
        }).count() == 1;
    }
    
    @JsonIgnore
    public boolean isAdmin(User user) {
        return hasGroup(user, DefaultGroup.admin);
    }

    @Override
    public String getOwnerId() {
        return getUserId();
    }

}
