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

import java.util.List;
import java.util.stream.Collectors;
import org.createnet.raptor.auth.service.RaptorUserDetailsService;
import org.createnet.raptor.auth.service.acl.RaptorPermission;
import org.createnet.raptor.auth.service.entity.Device;
import org.createnet.raptor.auth.service.entity.User;
import org.createnet.raptor.auth.service.exception.PermissionNotFoundException;
import org.createnet.raptor.auth.service.services.AclDeviceService;
import org.createnet.raptor.auth.service.services.DeviceService;
import org.createnet.raptor.auth.service.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.model.Permission;
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
@PreAuthorize("isAuthenticated()")
public class DevicePermissionController {

    static public class PermissionRequest {

        public String permission;
        public String user;

        public PermissionRequest() {
        }
    }

    static public class PermissionRequestBatch {

        public List<String> permissions;
        public String user;

        public PermissionRequestBatch() {
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(DevicePermissionController.class);

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private UserService userService;

    @Autowired
    private AclDeviceService aclDeviceService;

//  @RequestMapping(value = "/{deviceUuid}/permission", method = RequestMethod.POST)
//  public ResponseEntity<?> addPermission(
//          @RequestBody PermissionRequest body,
//          @PathVariable("deviceUuid") String deviceUuid
//  ) {
//
//    Device device = deviceService.getByUuid(deviceUuid);
//    if (device == null) {
//      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Device not found");
//    }
//
//    User user = userService.getByUuid(body.user);
//    if (user == null) {
//      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
//    }
//
//    Permission permission = RaptorPermission.fromLabel(body.permission);
//    if (permission == null) {
//      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Permission not recognized");
//    }
//
//    aclDeviceService.add(device, user, permission);
//    List<String> permissions = RaptorPermission.toLabel(aclDeviceService.list(device, user));
//
//    return ResponseEntity.status(HttpStatus.ACCEPTED).body(permissions);
//  }

    @RequestMapping(value = "/{deviceUuid}/permission/{userUuid}", method = RequestMethod.GET)
    public ResponseEntity<?> listPermissions(
            @PathVariable("deviceUuid") String deviceUuid,
            @PathVariable("userUuid") String userUuid
    ) {

        Device device = deviceService.getByUuid(deviceUuid);
        if (device == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Device not found");
        }

        User user = userService.getByUuid(userUuid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        List<String> permissions = RaptorPermission.toLabel(aclDeviceService.list(device, user));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(permissions);
    }

    @RequestMapping(value = "/{deviceUuid}/permission", method = RequestMethod.GET)
    public ResponseEntity<?> listOwnPermissions(
            @PathVariable("deviceUuid") String deviceUuid,
            @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails currentUser
    ) {

        Device device = deviceService.getByUuid(deviceUuid);
        if (device == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Device not found");
        }

        User user = userService.getByUuid(currentUser.getUuid());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        List<String> permissions = RaptorPermission.toLabel(aclDeviceService.list(device, user));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(permissions);
    }

    @RequestMapping(value = "/{deviceUuid}/permission", method = RequestMethod.PUT)
    public ResponseEntity<?> setPermission(
            @RequestBody PermissionRequestBatch body,
            @PathVariable("deviceUuid") String deviceUuid
    ) {

        Device device = deviceService.getByUuid(deviceUuid);
        if (device == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Device not found");
        }

        User user = userService.getByUuid(body.user);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        List<Permission> permissions = body.permissions
                .stream()
                .map((String s) -> {
                    Permission p = RaptorPermission.fromLabel(s);
                    if (p == null) {
                        throw new PermissionNotFoundException("Permission not found ");
                    }
                    return p;
                })
                .distinct()
                .collect(Collectors.toList());

        aclDeviceService.set(device, user, permissions);
        List<String> settedPermissions = RaptorPermission.toLabel(aclDeviceService.list(device, user));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(settedPermissions);
    }

}
