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
package org.createnet.raptor.auth.service.controller;

import java.util.Optional;
import org.createnet.raptor.auth.service.RaptorUserDetailsService;
import org.createnet.raptor.auth.service.entity.User;
import org.createnet.raptor.auth.service.entity.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@RestController
public class UserController {

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private UserRepository userRepository;

  @RequestMapping(value = "/users", method = RequestMethod.GET)
  public Iterable<User> getUsers(
          @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails user
  ) {
    
    if(!user.isAdmin()) {
      throw new AccessDeniedException("Cannot access this resource");
    }
    
    return userRepository.findAll();
  }

  @RequestMapping(value = {"/me", "/user/{uuid}"}, method = RequestMethod.GET)
  public User me(
          @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails user
  ) {
    User user1 = userRepository.findOne(user.getId());
    return user1;
  }

  @RequestMapping(value = {"/me", "/user/{uuid}", "/user"}, method = RequestMethod.PUT)
  public User update(
          @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails currentUser,
          @PathVariable Optional<String> uuidValue,
          @RequestBody User rawUser
  ) {

    String uuid = uuidValue.isPresent() ? uuidValue.get() : currentUser.getUuid();

    // TODO check user role or permissions
    if (!currentUser.isAdmin() && !uuid.equals(currentUser.getUuid())) {
      throw new AccessDeniedException("Cannot update user details");
    }

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
//    if (!rawUser.getRoles().isEmpty()) {
//      rawUser.getRoles().stream().forEach(r -> user.addRole(r));
//    }

    // TODO missing password validation
    if (rawUser.getPassword() != null && !rawUser.getPassword().isEmpty()) {
      user.setPassword(passwordEncoder.encode(rawUser.getPassword()));
    }

    User user1 = userRepository.save(user);
    return user1;
  }

}
