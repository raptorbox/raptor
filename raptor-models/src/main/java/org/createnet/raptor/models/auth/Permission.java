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

    static final Permission admin = new Permission("admin");
    static final Permission list = new Permission("list");
    
    static final Permission read = new Permission("read");
    static final Permission update = new Permission("update");
    static final Permission create = new Permission("create");
    static final Permission delete = new Permission("delete");

    static final Permission data = new Permission("data");    
    static final Permission push = new Permission("push");
    static final Permission pull = new Permission("pull");
    
    static final Permission execute = new Permission("execute");
    static final Permission tree = new Permission("tree");
    
    
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
    
}
