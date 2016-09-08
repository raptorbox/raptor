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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Role implements GrantedAuthority {
  
  public static enum Roles {
    ROLE_ADMIN, ROLE_USER
  }
  
  private static final long serialVersionUID = 1L;

  @JsonIgnore
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;
 
  @NotEmpty
  private Roles name;

  @JsonIgnore
  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "roles")
  private List<User> users = new ArrayList();

  @JsonIgnore
  @Override
  public String getAuthority() {
    return name.name();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof Roles) {
      return ((Roles)obj).equals(this.name);
    }
    return super.equals(obj); 
  }
  
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Roles getName() {
    return name;
  }

  public void setName(Roles name) {
    this.name = name;
  }

  public List<User> getUsers() {
    return users;
  }

  public void setUsers(List<User> users) {
    this.users = users;
  }

}
