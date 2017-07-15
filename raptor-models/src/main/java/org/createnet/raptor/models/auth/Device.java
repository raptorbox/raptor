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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.createnet.raptor.models.acl.AclSubject;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Entity
@Table(name = "devices")
public class Device implements AclSubject {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotNull
  private String uuid;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.EAGER)
  private User owner;

  @JsonIgnore
  @OneToOne(fetch = FetchType.LAZY)
  private Device parent;

  @JsonIgnore
  @OneToMany(mappedBy = "device", fetch = FetchType.LAZY)
  @Cascade(value = {CascadeType.REMOVE, CascadeType.SAVE_UPDATE})
  final private List<Token> tokens = new ArrayList();

  public Device() {
    this.uuid = UUID.randomUUID().toString();
  }
  
  public Device(String uuid) {
    this.uuid = uuid;
  }

  public Long getId() {
    return id;
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

  @Override
  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  public Device getParent() {
    return parent;
  }
  
  @JsonIgnore
  public Long getParentId() {
    return hasParent() ? getParent().getId() : null;
  }

  public void setParent(Device parent) {
    this.parent = parent;
  }

  public List<Token> getTokens() {
    return tokens;
  }

  public boolean hasParent() {
    return this.getParent() != null;
  }

  @Override
  public String toString() {
    return "Device{" + "uuid=" + uuid + '}';
  }

    @Override
    public Long getSubjectId() {
        return getId();
    }

    @Override
    public Long getSubjectParentId() {
        return getParentId();
    }
  
}
