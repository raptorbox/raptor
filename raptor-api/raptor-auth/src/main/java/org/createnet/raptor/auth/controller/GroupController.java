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
import org.createnet.raptor.models.auth.Group;
import org.createnet.raptor.auth.services.GroupService;
import org.createnet.raptor.models.auth.User;
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
 * @author Luca Capra <lcapra@fbk.eu>
 */
@RequestMapping(value = "/auth/role")
@RestController
@PreAuthorize("hasAuthority('super_admin')")
@Api(tags = {"Group"})
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
public class GroupController {

    private static final Logger logger = LoggerFactory.getLogger(GroupController.class);

    @Autowired
    private GroupService roleService;

    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(
            value = "List available roles",
            notes = "",
            response = Group.class,
            responseContainer = "Iterable",
            nickname = "getGroups"
    )
    public ResponseEntity<?> getGroups() {
        Iterable<Group> list = roleService.list();
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    @RequestMapping(value = {"/{roleId}"}, method = RequestMethod.PUT)
    @ApiOperation(
            value = "Update a role",
            notes = "",
            response = Group.class,
            nickname = "updateGroup"
    )
    public ResponseEntity<?> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long roleId,
            @RequestBody Group rawGroup
    ) {

        if ((rawGroup.getName().isEmpty() || rawGroup.getName() == null)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name property is missing");
        }

        Group role2 = roleService.getByName(rawGroup.getName());
        if (role2 != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }

        rawGroup.setId(roleId);
        Group role = roleService.update(roleId, rawGroup);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        logger.debug("Updated role {}", role.getName());
        return ResponseEntity.ok(role);
    }

    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(
            value = "Create a new role",
            notes = "",
            response = Group.class,
            nickname = "createGroup"
    )
    @ApiResponses(value = {
        @ApiResponse(
                code = 400,
                message = "Bad Request"
        )
        ,
        @ApiResponse(
                code = 409,
                message = "Conflict"
        )
    })
    public ResponseEntity<?> create(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Group rawGroup
    ) {

        if ((rawGroup.getName().isEmpty() || rawGroup.getName() == null)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name property is missing");
        }

        Group role2 = roleService.getByName(rawGroup.getName());
        if (role2 != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }

        Group role = roleService.create(rawGroup);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }

        logger.debug("Created role {}", role.getName());
        return ResponseEntity.ok(role);
    }

    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    @RequestMapping(value = {"/{roleId}"}, method = RequestMethod.DELETE)
    @ApiOperation(
            value = "Delete a role",
            notes = "",
            code = 202,
            nickname = "deleteGroup"
    )
    public ResponseEntity<Group> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long roleId
    ) {

        if (!roleService.delete(roleId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        logger.debug("Deleted role {}", roleId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

}
