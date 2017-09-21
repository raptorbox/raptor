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

import java.util.Set;

import javax.persistence.Id;
import org.hibernate.validator.constraints.NotEmpty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Email;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */

@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"}, ignoreUnknown = true)
@Document
//@Cacheable(value = true)
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "users")
public class User implements Serializable {

    @JsonIgnore
    @Id
    protected String id;

    @NotEmpty
    protected String uuid = UUID.randomUUID().toString();

    @NotEmpty
    @Size(min = 4, max = 128)
    protected String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotEmpty
    @Size(min = 4, max = 128)
    protected String password;

    @JsonIgnore
    final protected List<Token> tokens = new ArrayList();

    final protected List<Role> roles = new ArrayList();

    @Size(min = 4, max = 64)
    protected String firstname;

    @Size(min = 4, max = 64)
    protected String lastname;

    @NotNull
    @Email
    protected String email;

    @NotNull
    protected boolean enabled = true;

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    protected Date lastPasswordResetDate = new Date();

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
        user.getRoles().stream().forEach((role) -> this.addRole(role));

        if (!newUser) {
            this.id = user.getId();
            this.uuid = user.getUuid();
        }

    }

    @JsonIgnore
    public boolean isSuperAdmin() {
        return this.hasRole(Role.super_admin);
    }

    public boolean hasRole(Role role) {
        return this.getRoles().contains(role);
    }

    public boolean hasRole(String name) {
        return hasRole(new Role(name));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    @JsonProperty("roles")
    public Object[] listRoles() {
        return roles.stream().map(r -> r.getName()).toArray();
    }

    @JsonProperty("roles")
    public void setListRoles(List<String> list) {
        list.forEach(r -> this.addRole(new Role(r)));
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
    }

    public void addRole(Role role) {
        if (!this.hasRole(role)) {
            this.roles.add(role);
        }
    }

    public void removeRole(Role role) {
        if (this.hasRole(role)) {
            this.roles.remove(role);
        }
    }

    public void removeRole(String role) {
        this.removeRole(new Role(role));
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

    @Override
    public String toString() {
        return "User{" + "uuid=" + uuid + '}';
    }

}
