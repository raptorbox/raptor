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


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import org.hibernate.validator.constraints.NotEmpty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.Cacheable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.validator.constraints.Email;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"}, ignoreUnknown = true)
@Entity
@Cacheable(value = true)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "users")
public class User implements Serializable {

    static final long serialVersionUID = 1000000000000001L;

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    @NotEmpty
    protected String uuid = UUID.randomUUID().toString();

    @NotEmpty
    @Column(unique = true, nullable = false, length = 128)
    @Size(min = 4, max = 128)
    protected String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotEmpty
    @Column(length = 128)
    @Size(min = 4, max = 128)
    protected String password;

    @JsonIgnore
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    @Cascade(value = {CascadeType.REMOVE, CascadeType.SAVE_UPDATE})
    final protected List<AclDevice> devices = new ArrayList();

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Cascade(value = {CascadeType.REMOVE, CascadeType.SAVE_UPDATE})
    final protected List<Token> tokens = new ArrayList();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_groups", joinColumns = {
        @JoinColumn(name = "user_id")}, inverseJoinColumns = {
        @JoinColumn(name = "group_id")})
    final protected List<Group> groups = new ArrayList();

    @Column(length = 64)
    @Size(min = 4, max = 64)
    protected String firstname;

    @Column(length = 64)
    @Size(min = 4, max = 64)
    protected String lastname;

    @Column(length = 128)
    @NotNull
    @Email
    protected String email;

    @Column()
    @NotNull
    protected boolean enabled = true;

    @JsonIgnore
    @Column(name = "last_password_reset")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    protected Date lastPasswordResetDate = new Date();

    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    protected Date created = new Date();

    public User() {
    }

    public User(User user) {
        this(user, false);
    }

    public User(User user, boolean newUser) {

        super();

        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.enabled = user.getEnabled();

        user.getTokens().stream().forEach((token) -> this.addToken(token));
        user.getGroups().stream().forEach((g) -> this.addGroup(g));

        if (!newUser) {
            this.id = user.getId();
            this.uuid = user.getUuid();
        }

    }

    @JsonIgnore
    public boolean isAdmin() {
        return this.hasGroup(StaticGroup.admin);
    }

    public boolean hasGroup(String name) {
        return this.getGroups().stream().filter(r -> r.getName().equals(name)).count() >= 1;
    }

    public boolean hasGroup(StaticGroup g) {
        return hasGroup(g.name());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JsonProperty("groups")
    public Object[] listGroups() {
        return groups.stream().map(g -> g.getName()).toArray();
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups.clear();
        this.groups.addAll(groups);
    }

    public void addGroup(Group role) {
        if (!this.hasGroup(role.getName())) {
            this.groups.add(role);
        }
    }

    public void addGroup(StaticGroup g) {
        if (!this.hasGroup(g)) {
            this.groups.add(new Group(g));
        }
    }

    public void removeGroup(Group role) {
        if (this.hasGroup(role.getName())) {
            this.groups.remove(role);
        }
    }

    public void removeGroup(StaticGroup g) {
        if (this.hasGroup(g)) {
            this.groups.remove(new Group(g));
        }
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens.clear();
        this.tokens.addAll(tokens);
    }

    public void addToken(Token token) {
        if (!this.tokens.contains(token)) {
            this.tokens.add(token);
        }
    }

    public void removeToken(Token token) {
        if (this.tokens.contains(token)) {
            this.tokens.remove(token);
        }
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Date getLastPasswordResetDate() {
        return lastPasswordResetDate;
    }

    public void setLastPasswordResetDate(Date lastPasswordResetDate) {
        this.lastPasswordResetDate = lastPasswordResetDate;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public List<AclDevice> getDevices() {
        return devices;
    }

    @Override
    public String toString() {
        return "User{" + "uuid=" + uuid + '}';
    }

    public boolean hasPermission(Permission p) {
        return getGroups().stream().filter((g) -> {
            return g.getPermissions().contains(p);
        }).count() > 0;
    }
    
    public boolean hasPermission(String p) {
        return hasPermission(new Permission(p));
    }

}
