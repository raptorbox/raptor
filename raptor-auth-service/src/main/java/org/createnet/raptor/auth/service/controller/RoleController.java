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

import org.createnet.raptor.auth.service.RaptorUserDetailsService;
import org.createnet.raptor.models.auth.Role;
import org.createnet.raptor.auth.service.services.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@PreAuthorize("hasAuthority('super_admin')")
public class RoleController {

  private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

  @Autowired
  private RoleService roleService;

  @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
  @RequestMapping(value = "/roles", method = RequestMethod.GET)
  public ResponseEntity<?> getUsers() {
    Iterable<Role> list = roleService.list();
    return ResponseEntity.ok(list);
  }

  @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
  @RequestMapping(value = {"/roles/{roleId}"}, method = RequestMethod.PUT)
  public ResponseEntity<?> update(
          @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails currentUser,
          @PathVariable Long roleId,
          @RequestBody Role rawRole
  ) {

    if ((rawRole.getName().isEmpty() || rawRole.getName() == null)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name property is missing");
    }

    Role role2 = roleService.getByName(rawRole.getName());
    if (role2 != null) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
    }

    rawRole.setId(roleId);
    Role role = roleService.update(roleId, rawRole);
    if (role == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    logger.debug("Updated role {}", role.getName());
    return ResponseEntity.ok(role);
  }

  @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
  @RequestMapping(value = {"/roles"}, method = RequestMethod.POST)
  public ResponseEntity<?> create(
          @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails currentUser,
          @RequestBody Role rawRole
  ) {

    if ((rawRole.getName().isEmpty() || rawRole.getName() == null)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name property is missing");
    }

    Role role2 = roleService.getByName(rawRole.getName());
    if (role2 != null) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
    }

    Role role = roleService.create(rawRole);
    if (role == null) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
    }

    logger.debug("Created role {}", role.getName());
    return ResponseEntity.ok(role);
  }

  @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
  @RequestMapping(value = {"/roles/{roleId}"}, method = RequestMethod.DELETE)
  public ResponseEntity<Role> delete(
          @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails currentUser,
          @PathVariable Long roleId
  ) {

    if (!roleService.delete(roleId)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    logger.debug("Deleted role {}", roleId);
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
  }

}
