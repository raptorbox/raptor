/*
 * Copyright 2017 FBK/CREATE-NET <http://create-net.fbk.eu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.createnet.raptor.models.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Permission {

    public static final Permission admin = new Permission("admin");
    public static final Permission list = new Permission("list");
    
    public static final Permission read = new Permission("read");
    public static final Permission update = new Permission("update");
    public static final Permission create = new Permission("create");
    public static final Permission delete = new Permission("delete");

    public static final Permission data = new Permission("data");    
    public static final Permission push = new Permission("push");
    public static final Permission pull = new Permission("pull");
    
    public static final Permission execute = new Permission("execute");
    public static final Permission tree = new Permission("tree");
    
    public Permission() {
    }
    
    public Permission(String name) {
        this.name = name;
    }
    
    public Permission(String applicationId, String name) {
        this.applicationId = applicationId;
        this.name = name;
    }
    
    @Indexed
    protected String applicationId;
    
    @Indexed
    protected String name;
    
    protected String description;

    @Override
    public String toString() {
        return this.getName();
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
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
    
}
