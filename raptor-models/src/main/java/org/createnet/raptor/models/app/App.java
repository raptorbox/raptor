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
import java.util.List;
import java.util.UUID;
import org.createnet.raptor.models.auth.Role;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@Document
public class App {

    @Id
    protected String id = UUID.randomUUID().toString();

    protected String userId;
    protected String name;
    protected String description;

    protected List<AppRole> roles = new ArrayList();
    protected List<AppUser> users = new ArrayList();

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

    public List<AppRole> getRoles() {
        return roles;
    }

    public void setRoles(List<AppRole> roles) {
        this.roles = roles;
    }

    public List<AppUser> getUsers() {
        return users;
    }

    public void setUsers(List<AppUser> users) {
        this.users = users;
    }

}
