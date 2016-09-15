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
package org.createnet.raptor.auth.service.services;

import org.createnet.raptor.auth.service.entity.Role;
import org.createnet.raptor.auth.service.entity.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class RoleService {
  
  private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

  @Autowired
  private RoleRepository roleRepository;

  public Role update(Long roleId, Role rawRole) {

    Role role = roleRepository.findOne(roleId);
    
    if(role == null) {
      return null;
    } 
   
    if (rawRole.getName() != null && !rawRole.getName().isEmpty()) {
      role.setName(rawRole.getName());
    }

    return roleRepository.save(role);
  }

  public Iterable<Role> list() {
    return roleRepository.findAll();
  }

  public Role create(Role rawRole) {

    Role existsRole = roleRepository.findByName(rawRole.getName());
    if (existsRole != null) {
      return null;
    }
    
    Role role = new Role();
    role.setName(rawRole.getName());
    
    return roleRepository.save(role);
  }
  
  public boolean delete(Long roleId) {
    
    if (!roleRepository.exists(roleId)) {
      return false;
    }

    roleRepository.delete(roleId);
    return true;
  }
  
}
