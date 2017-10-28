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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import javax.persistence.Cacheable;
import javax.persistence.Table;
import org.createnet.raptor.models.acl.EntityType;
import org.createnet.raptor.models.acl.Operation;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cacheable(value = true)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "permissions")
public class Permission implements GrantedAuthority {

    private static final long serialVersionUID = 1000000000000003L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotEmpty
    private String name;

    public Permission() {
    }

    public Permission(String name) {
        this.name = name;
    }

    public Permission(Operation name) {
        this.name = name.name();
    }

    public Permission(EntityType entity, Operation operation) {
        buildName(entity, operation, false);
    }

    public Permission(EntityType entity, Operation operation, boolean own) {
        buildName(entity, operation, own);
    }

    public void buildName(EntityType entity, Operation operation, boolean own) {
        this.name = entity.name() + "_" + operation.name();
        if (own) {
            this.name += "_own";
        }
    }

    @JsonIgnore
    @Override
    public String getAuthority() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Permission) {
            return ((Permission) obj).getName().equals(this.name);
        }
        if (obj instanceof String) {
            return ((String) obj).equals(this.name);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.name);
        return hash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName() + "[id=" + getId() + "]";
    }

}
