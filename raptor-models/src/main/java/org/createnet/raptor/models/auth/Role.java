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
package org.createnet.raptor.models.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Id;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;

//@Cacheable(value = true)
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Role implements GrantedAuthority {
    
    final static public Role super_admin = new Role("super_admin");
    static public Role user = new Role("user", defaultUserPermissions());
    static public Role admin = new Role("admin", defaultUserPermissions());
    
    public Role(String name) {
        this.name = name;
    }
    
    public Role(String name, List<Permission> permissions) {
        this.name = name;
        this.permissions.addAll(permissions);
    }
    
    @NotEmpty
    @Id
    private String name;
    
    private String description;
    
    private final List<Permission> permissions = new ArrayList();

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

    public List<Permission> getPermissions() {
        return permissions;
    }
    
    static protected List<Permission> defaultUserPermissions() {
        return Arrays.asList(
                Permission.create,
                Permission.read,
                Permission.update,
                Permission.delete,
                Permission.push,
                Permission.pull,
                Permission.execute,
                Permission.tree
        );
    }
    
    static protected List<Permission> defaultAdminPermissions() {
        return Arrays.asList(Permission.admin);
    }

    @Override
    public String getAuthority() {
        return this.name;
    }
    
}
