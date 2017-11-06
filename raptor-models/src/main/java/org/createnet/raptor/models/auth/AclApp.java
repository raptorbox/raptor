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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.createnet.raptor.models.acl.AbstractAclSubject;
import org.createnet.raptor.models.app.App;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"}, ignoreUnknown = true)
@Entity
@Cacheable(value = true)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "apps")
public class AclApp extends AbstractAclSubject {

    static final long serialVersionUID = 1000000000000005L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private String uuid;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    private User owner;

    @OneToMany(mappedBy = "app", fetch = FetchType.LAZY)    
    private List<Role> groups = new ArrayList();

    public AclApp() {
    }

    public AclApp(Long id, String uuid, User owner) {
        this.id = id;
        this.uuid = uuid;
        this.owner = owner;
    }
    
    public AclApp(App app) {
        this.uuid = app.getId();
        this.owner = new User(app.getUserId());
    }
    
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public User getUser() {
        return getOwner();
    }
    
    public User getOwner() {
        return owner;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return String.format("App[uuid=%s]", uuid);
    }

    public List<Role> getGroups() {
        return groups;
    }

    public void setGroups(List<Role> groups) {
        this.groups = groups;
    }

}
