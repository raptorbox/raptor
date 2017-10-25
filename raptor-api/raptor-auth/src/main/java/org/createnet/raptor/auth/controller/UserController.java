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
package org.createnet.raptor.auth.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.createnet.raptor.auth.events.UserEventPublisher;
import org.createnet.raptor.auth.services.TokenService;
import org.createnet.raptor.models.response.JsonErrorResponse;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.auth.services.UserService;
import org.createnet.raptor.models.auth.Role;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.request.LoginResponse;
import org.createnet.raptor.models.configuration.RaptorConfiguration;
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
 * @author Luca Capra <lcapra@fbk.eu>
 */
@RequestMapping(value = "/auth/user")
@RestController
@PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
@Api(tags = {"User"})
@ApiResponses(value = {
    @ApiResponse(
            code = 200,
            message = "Ok"
    )
    ,
    @ApiResponse(
            code = 401,
            message = "Not authorized"
    )
    ,
    @ApiResponse(
            code = 403,
            message = "Forbidden"
    )
    ,
    @ApiResponse(
            code = 500,
            message = "Internal error"
    )
})
public class UserController {

    @Autowired
    private UserEventPublisher eventPublisher;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RaptorConfiguration configuration;

    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(
            value = "List available user",
            notes = "",
            response = User.class,
            responseContainer = "Iterable",
            nickname = "getUsers"
    )
    public Iterable<User> getUsers() {
        return userService.list();
    }

    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(
            value = "Create a new user",
            notes = "",
            response = User.class,
            nickname = "createUser"
    )
    public ResponseEntity<?> createUser(
            @AuthenticationPrincipal User currentUser,
            @RequestBody User rawUser
    ) {

        boolean exists = userService.exists(rawUser);
        if (exists) {
            return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST, "Username already taken");
        }

        User saved = userService.create(new User(rawUser, true));
        eventPublisher.create(saved);
        
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("(hasAuthority('admin') or hasAuthority('super_admin')) or principal.uuid == #uuid")
    @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
    @ApiOperation(
            value = "Get an user profile",
            notes = "",
            response = User.class,
            nickname = "getUser"
    )
    public ResponseEntity getUser(
            @PathVariable String uuid,
            @AuthenticationPrincipal User currentUser
    ) {

        User u = userService.getByUuid(uuid);

        if (u == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "User not found");
        }

        return ResponseEntity.ok(u);
    }

    @PreAuthorize("(hasAuthority('admin') or hasAuthority('super_admin')) or principal.uuid == #uuid")
    @RequestMapping(value = "/{uuid}/impersonate", method = RequestMethod.GET)
    @ApiOperation(
            value = "Retrieve a login token for the user",
            notes = "",
            response = LoginResponse.class,
            nickname = "impersonateUser"
    )
    public ResponseEntity impersonateUser(
            @PathVariable String uuid,
            @AuthenticationPrincipal User currentUser
    ) {

        User u = userService.getByUuid(uuid);
        if (u == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "User not found");
        }

        if (u.hasRole(Role.Roles.super_admin)) {
            return JsonErrorResponse.entity(HttpStatus.FORBIDDEN, "Cannot impersonat this user");
        }

        if (configuration.getAuth().userHasLock(u.getUsername())) {
            return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST, "User cannot be modified");
        }

        final Token token = tokenService.createLoginToken(u);
        return ResponseEntity.ok(new LoginResponse(u, token));
    }

    @PreAuthorize("(hasAuthority('admin') or hasAuthority('super_admin')) or principal.uuid == #uuid")
    @RequestMapping(value = "/{uuid}", method = RequestMethod.PUT)
    @ApiOperation(
            value = "Update an user profile",
            notes = "",
            response = User.class,
            nickname = "updateUser"
    )
    public ResponseEntity<?> updateUser(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("uuid") String uuid,
            @RequestBody User rawUser
    ) {

        User user = userService.getByUuid(uuid);

        if (user == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "Requested user not found");
        }

        if (rawUser.getUuid() != null && !user.getUuid().equals(rawUser.getUuid())) {
            return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST, "Path and body uuid mismatch");
        }

        // change username, but ensure it does not exists already
        if (!rawUser.getUsername().equals(user.getUsername())) {
            User exists = userService.findByUsername(rawUser.getUsername());
            if (exists != null) {
                return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST, "Username already taken");
            }
        }

        // change email, but ensure it does not exists already
        if (!rawUser.getEmail().equals(user.getEmail())) {
            User exists = userService.findByEmail(rawUser.getEmail());
            if (exists != null) {
                return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST, "Email already registered");
            }
        }

        if (configuration.getAuth().userHasLock(rawUser.getUsername())) {
            return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST, "User cannot be modified");
        }

        User saved = userService.update(user, rawUser);

        eventPublisher.update(saved);

        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("(hasAuthority('admin') or hasAuthority('super_admin')) or principal.uuid == #uuid")
    @RequestMapping(value = "/{uuid}", method = RequestMethod.DELETE)
    @ApiOperation(
            value = "Delete an user profile",
            notes = "",
            code = 202,
            nickname = "deleteUser"
    )
    public ResponseEntity deleteUser(@PathVariable String uuid) {

        User user = userService.getByUuid(uuid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }

        if (configuration.getAuth().userHasLock(user.getUsername())) {
            return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST, "User cannot be modified");
        }

        userService.delete(user);

        eventPublisher.delete(user);

        return ResponseEntity.accepted().body(null);
    }

}
