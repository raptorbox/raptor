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

import java.util.stream.Collectors;
import org.createnet.raptor.auth.service.entity.Role;
import org.createnet.raptor.auth.service.entity.User;
import org.createnet.raptor.auth.service.entity.repository.RoleRepository;
import org.createnet.raptor.auth.service.entity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class UserService {

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  public Iterable<User> list() {
    return userRepository.findAll();
  }

  public User save(User user) {
    saveRoles(user);
    return userRepository.save(user);
  }
  
  public User getByUuid(String uuid) {
    return userRepository.findByUuid(uuid);
  }

  /**
   * Save new roles or load roles based on name to ensure roles are all managed
   */
  protected void saveRoles(User user) {
    user.setRoles(
            user.getRoles().stream().map((r) -> {

              if (r.getId() != null) {
                return r;
              }

              Role r1 = roleRepository.findByName(r.getName());

              if (r1 != null) {
                return r1;
              }

              r = roleRepository.save(r);
              return r;
            }).collect(Collectors.toSet())
    );
  }

  public User update(String uuid, User rawUser) {

    User user = userRepository.findByUuid(uuid);

    if (rawUser.getFirstname() != null && !rawUser.getFirstname().isEmpty()) {
      user.setFirstname(rawUser.getFirstname());
    }

    if (rawUser.getLastname() != null && !rawUser.getLastname().isEmpty()) {
      user.setLastname(rawUser.getLastname());
    }

    if (rawUser.getEmail() != null && !rawUser.getEmail().isEmpty()) {
      user.setEmail(rawUser.getEmail());
    }

    if (rawUser.getEnabled() != null) {
      user.setEnabled(rawUser.getEnabled());
    }

//      // TODO add Role repository
    if (!rawUser.getRoles().isEmpty()) {
      rawUser.getRoles().stream().forEach(r -> user.addRole(r));
      saveRoles(user);
    }

    // TODO missing password validation
    if (rawUser.getPassword() != null && !rawUser.getPassword().isEmpty()) {
      user.setPassword(passwordEncoder.encode(rawUser.getPassword()));
    }

    return userRepository.save(user);    
  }

  public User create(User rawUser) {
    return userRepository.save(rawUser);
  }

  public void delete(String uuid) {
    User user = getByUuid(uuid);
    if(user == null) {
      return;
    }
    userRepository.delete(user.getId());
  }
}
