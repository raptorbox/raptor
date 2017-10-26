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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Cacheable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cacheable(value = true)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "groups")
public class Group implements Serializable {

    private static final long serialVersionUID = 1000000000000004L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotEmpty
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private AclApp app;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "groups_permissions", joinColumns = {
        @JoinColumn(name = "group_id")}, inverseJoinColumns = {
        @JoinColumn(name = "permission_id")})
    private List<Permission> permissions = new ArrayList();

    public Group() {
    }

    public Group(String name) {
        this.name = name;
    }

    public Group(String name, List<Permission> permissions) {
        this.name = name;
        this.permissions.addAll(permissions);
    }

    public Group(String name, AclApp app) {
        this(name);
        this.app = app;
    }

    public Group(DefaultGroup g) {
        this.name = g.name();
    }

    public Group(DefaultGroup g, List<Permission> permissions) {
        this(g.name(), permissions);
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

    @JsonProperty
    public String getAppId() {
        return getApp() != null ? getApp().getUuid(): null;
    }
    
    @JsonProperty
    public void setAppId(String appId) {
        
        if (getApp() == null) {
            setApp(new AclApp());
        }
        
        getApp().setUuid(appId);
    }
    
    @JsonIgnore
    public AclApp getApp() {
        return app;
    }

    public void setApp(AclApp app) {
        this.app = app;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public void merge(Group raw) {

        if (raw.getName() != null && !raw.getName().isEmpty()) {
            this.setName(raw.getName());
        }

        setApp(raw.getApp());

        raw.getPermissions().forEach((p) -> {
            if (!getPermissions().contains(p)) {
                getPermissions().add(p);
            }
        });

    }

}
