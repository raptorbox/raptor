/*
 * Copyright 2016 CREATE-NET
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
package org.createnet.raptor.auth.service.entity;

import java.util.Set;

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
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.validator.constraints.Email;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

  @JsonIgnore
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotEmpty
  private String uuid = UUID.randomUUID().toString();

  @NotEmpty
  @Column(unique = true, nullable = false, length = 256)
  @Size(min = 4, max = 256)
  private String username;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @NotEmpty
  @Column(length = 128)
  @Size(min = 4, max = 128)
  private String password;

  @JsonIgnore
  @OneToMany(mappedBy = "user", fetch= FetchType.EAGER, orphanRemoval = true)
  @Cascade(value = { CascadeType.REMOVE, CascadeType.SAVE_UPDATE })
  final private List<Token> tokens = new ArrayList();

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "users_roles", joinColumns = {
    @JoinColumn(name = "user_id")}, inverseJoinColumns = {
    @JoinColumn(name = "role_id")})
  final private List<Role> roles = new ArrayList();

  @Column(length = 64)
  @Size(min = 4, max = 64)
  private String firstname;

  @Column(length = 64)
  @Size(min = 4, max = 64)
  private String lastname;

  @Column(length = 128)
  @NotNull
  @Email
  private String email;

  @Column()
  @NotNull
  private boolean enabled = true;

  @JsonIgnore
  @Column(name = "last_password_reset")
  @Temporal(TemporalType.TIMESTAMP)
  @NotNull
  private Date lastPasswordResetDate = new Date();

  @Column(name = "created")
  @Temporal(TemporalType.TIMESTAMP)
  @NotNull
  private Date created = new Date();

  public User() {
  }

  public User(User user) {

    super();

    this.id = user.getId();
    this.uuid = user.getUuid();
    this.username = user.getUsername();
    this.password = user.getPassword();

    user.getTokens().stream().forEach((token) -> this.addToken(token));
    user.getRoles().stream().forEach((role) -> this.addRole(role));

  }

  @JsonIgnore
  public boolean isAdmin() {
    return isSuperAdmin() || this.hasRole(Role.Roles.admin);
  }
  
  @JsonIgnore
  public boolean isSuperAdmin() {
    return this.hasRole(Role.Roles.super_admin);
  }
  
  public boolean hasRole(Role.Roles name) {
    return hasRole(name.name());
  }
  
  public boolean hasRole(String name) {
    return this.getRoles().stream().filter(r -> r.getName().equals(name)).count() >= 1;
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

  public List<Role> getRoles() {
    return roles;
  }

  public void setRoles(Set<Role> roles) {
    this.roles.clear();
    this.roles.addAll(roles);
  }

  public void addRole(Role role) {
    if (!this.hasRole(role.getName())) {
      this.roles.add(role);
    }
  }
  
  public void addRole(Role.Roles role) {
    if (!this.hasRole(role)) {
      this.roles.add(new Role(role));
    }
  }

  public void removeRole(Role role) {
    if (this.hasRole(role.getName())) {
      this.roles.remove(role);
    }
  }
  
  public void removeRole(Role.Roles role) {
    if (this.hasRole(role)) {
      this.roles.remove(new Role(role));
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

}
