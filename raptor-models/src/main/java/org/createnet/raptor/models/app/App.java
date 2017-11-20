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
import java.util.UUID;
import java.util.stream.Collectors;
import org.createnet.raptor.models.acl.Owneable;
import org.createnet.raptor.models.auth.Permission;
import org.createnet.raptor.models.auth.User;
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
    protected boolean enabled;

    final protected List<AppRole> roles = new ArrayList();
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
        this(name, owner.getId());
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
        if (raw.getRoles() != null && !raw.getRoles().isEmpty()) {
            setRoles(raw.getRoles());
        }
        if (raw.getUsers() != null && !raw.getUsers().isEmpty()) {
            setUsers(raw.getUsers());
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
            u.getRoles().forEach((rolename) -> {
                if (!getRoles().contains(new AppRole(rolename))) {
                    throw new RaptorComponent.ValidationException(String.format("User `%s` has an unknown app role `%s`. Add to the app list to allow it", u.getId(), rolename));
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

    public List<AppRole> getRoles() {
        return roles;
    }

    public void setRoles(List<AppRole> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
    }

    public void setOwner(User user) {
        this.userId = user.getId();
    }

    public void addUser(User user, String role) {
        addUser(user, Arrays.asList(role));
    }
    
    public void addUser(User user, List<String> roles) {

        AppUser appUser = new AppUser();
        appUser.setId(user.getId());
        appUser.addRoles(roles);

        this.getUsers().add(appUser);
    }
    
    public void addRoles(List<AppRole> roles) {
        roles.forEach((r) -> {
            if (getRoles().contains(r)) {
                getRoles().remove(r);
            }
            getRoles().add(r);
        });
    }

    public void addRole(AppRole r) {
        addRoles(Arrays.asList(r));
    }

    public void addRole(String r, List<String> permissions) {
        addRole(new AppRole(name, permissions));
    }

    public void removeRole(String role) {
        getRoles().forEach((r) -> {
            if (r.getName().equals(role)) {
                getRoles().remove(r);
            }
        });
    }

    public boolean hasRole(User user, String role) {
        return getUsers().stream().anyMatch((u) -> u.getId().equals(user.getId()) && u.hasRole(role));
    }

    public boolean isAdmin(User user) {
        return hasRole(user, "admin");
    }
    
    public boolean isService(User user) {
        return hasRole(user, "service");
    }

    @JsonIgnore
    @Override
    public String getOwnerId() {
        return getUserId();
    }

    public boolean hasPermission(String p) {
        return getRoles()
                .stream()
                .anyMatch((g) -> g.getPermissions().contains(p));
    }
    
    public boolean hasPermission(Permission p) {
        return hasPermission(p.toString());
    }
    
    public List<AppRole> getRolesByPermission(String p) {
        return getRoles()
                .stream()
                .filter((g) -> {
                    return g.getPermissions().contains(p);
                })
                .distinct()
                .collect(Collectors.toList());
    }

    public AppUser getUser(User user) {
        for (AppUser u : users) {
            if(u.getId().equals(user.getId())) return u;
        }
        return null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
}
