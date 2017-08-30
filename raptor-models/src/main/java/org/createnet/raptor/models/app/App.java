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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Document
public class App {
    
    @Id
    protected String id = UUID.randomUUID().toString();
    
    @Indexed
    protected String userId;
    
    @Indexed
    protected String name;
    protected String description;
    
    protected final Map<String, Object> options = new HashMap();
    protected final List<User> users = new ArrayList();
    protected final List<Device> devices = new ArrayList();
    protected final List<Token> tokens = new ArrayList();

    public App() {
    }

    public void validate() throws RaptorComponent.ValidationException {
        
        if (getName() == null || getName().isEmpty()) {
            throw new RaptorComponent.ValidationException("name is required");
        }
        
        if (getId()== null || getId().isEmpty()) {
            throw new RaptorComponent.ValidationException("ID is required");
        }
        
        if (getUserId()== null || getUserId().isEmpty()) {
            throw new RaptorComponent.ValidationException("user ID is required");
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

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options.putAll(options);
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users.addAll(users);
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices.addAll(devices);
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens.addAll(tokens);
    }

}
