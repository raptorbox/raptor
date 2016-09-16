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
import org.createnet.raptor.auth.service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
public class UserController {

  @Autowired
  private UserService userService;

  @RequestMapping(value = "/users", method = RequestMethod.GET)
  public Iterable<User> getUsers() {
    return userService.list();
  }

  @RequestMapping(value = {"/user",}, method = RequestMethod.GET)
  public User me(
          @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails user
  ) {
    return (User) user;
  }

  @PreAuthorize("(hasAuthority('admin') or hasAuthority('super_admin')) or principal.uuid == #uuid")
  @RequestMapping(value = {"/user/{uuid}"}, method = RequestMethod.GET)
  public User readUser(
          @PathVariable String uuid,
          @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails currentUser
  ) {
    return userService.getByUuid(uuid);
  }

  @PreAuthorize("(hasAuthority('admin') or hasAuthority('super_admin')) or principal.uuid == #uuid")
  @RequestMapping(value = {"/user"}, method = RequestMethod.PUT)
  public User updateMe(
          @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails currentUser,
          @RequestBody User rawUser
  ) {
    return userService.update(currentUser.getUuid(), rawUser);
  }

  @PreAuthorize("(hasAuthority('admin') or hasAuthority('super_admin')) or principal.uuid == #uuid")
  @RequestMapping(value = {"/user/{uuid}"}, method = RequestMethod.PUT)
  public User update(
          @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails currentUser,
          @PathVariable Optional<String> uuidValue,
          @RequestBody User rawUser
  ) {
    String uuid = uuidValue.isPresent() ? uuidValue.get() : currentUser.getUuid();
    return userService.update(uuid, rawUser);
  }

  @PreAuthorize("(hasAuthority('admin') or hasAuthority('super_admin')) or principal.uuid == #uuid")
  @RequestMapping(value = {"/user/{uuid}"}, method = RequestMethod.PUT)
  public ResponseEntity<String> delete(
          @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails currentUser,
          @PathVariable String uuid,
          @RequestBody User rawUser
  ) {
    userService.delete(uuid);
    return ResponseEntity.accepted().body(null);
  }

  @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
  @RequestMapping(value = {"/user"}, method = RequestMethod.POST)
  public User create(
          @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails currentUser,
          @RequestBody User rawUser
  ) {
    return userService.create(rawUser);
  }
  
}
