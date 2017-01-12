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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Entity
@Table(name = "tokens")
public class Token implements Serializable {

  public static enum Type {
    LOGIN, DEFAULT
  }
  
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotEmpty
  private String name;

  @NotEmpty
  @Column(unique = true, nullable = false)
  private String token;
  
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Column(unique = false, nullable = true)
  private String secret;
  
  @Column(unique = false, nullable = false)
  private boolean enabled = false;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.EAGER)
  private User user;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.EAGER)
  private Device device;

  @Column(name = "created")
  @Temporal(TemporalType.TIMESTAMP)
  @NotNull  
  private Date created = new Date();
  
  @Column(name = "expires")
  private Long expires = null;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  private Type type = Type.DEFAULT;
  
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

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public Date getCreated() {
    return created;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public Instant getExpiresInstant() {
    if(expires == null)
      return null;
    return Instant.now().plusSeconds(expires);
  }
  
  public Long getExpires() {
    return expires;
  }

  public void setExpires(Long expires) {
    this.expires = expires;
  }
  
  public boolean isExpired() {
    return getExpiresInstant() != null && getExpiresInstant().isBefore(Instant.now());
  }

  public boolean isEnabled() {
    return enabled;
  }
  
  public boolean isValid() {
    return isEnabled() && !isExpired() && (getUser() != null && getUser().isEnabled());
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }
  
  public boolean isLoginToken() {
    return this.getType().equals(Type.LOGIN);
  }

  public Device getDevice() {
    return device;
  }

  public void setDevice(Device device) {
    this.device = device;
  }

}
