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
package org.createnet.raptor.models.profile;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.createnet.raptor.models.objects.Device;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Document
public class UserPreference {
    
    public static class Value {
    }
    
    @Id
    protected String id = UUID.randomUUID().toString();
    
    protected String userId;
    protected String name;
    protected String value;

    public UserPreference() {
    }
    
    public UserPreference(String userId, String name, String value) {
        this.userId = userId;
        this.name = name;
        this.value = value;
        generateId();
    }
    
    protected void generateId() {
        this.id = String.format("%s_%s", userId, name);
    }
    
    public String getId() {
        return id;
    }
    
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        generateId();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        generateId();
    }
    
}
