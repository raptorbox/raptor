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
import org.createnet.raptor.models.response.JsonErrorResponse;
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
@RequestMapping(value = "/auth/group")
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
    private GroupService groupService;

    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(
            value = "List available groups",
            notes = "",
            response = Group.class,
            responseContainer = "Iterable",
            nickname = "getGroups"
    )
    public ResponseEntity<?> getGroups() {
        Iterable<Group> list = groupService.list();
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasRole('admin') or hasRole('group_admin') or hasRole('group_update')")
    @RequestMapping(value = {"/{groupId}"}, method = RequestMethod.PUT)
    @ApiOperation(
            value = "Update a group",
            notes = "",
            response = Group.class,
            nickname = "updateGroup"
    )
    public ResponseEntity<?> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long groupId,
            @RequestBody Group raw
    ) {

        if ((raw.getName() == null || raw.getName().isEmpty())) {
            return JsonErrorResponse.badRequest("Missing group name");
        }

        Group group = groupService.getById(groupId);
        if (group == null) {
            return JsonErrorResponse.notFound();
        }

        group.merge(raw);

        group = groupService.save(group);
        if (group == null) {
            return JsonErrorResponse.internalError("Failed to store group");
        }

        logger.debug("Updated group {} [id={}]", group.getName(), group.getId());
        return ResponseEntity.ok(group);
    }

    @PreAuthorize("hasRole('admin')")
    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(
            value = "Create a new group",
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
            @RequestBody Group raw
    ) {

        if ((raw.getName() == null || raw.getName().isEmpty())) {
            return JsonErrorResponse.badRequest("Missing group name");
        }

        Group group = groupService.getByName(raw.getName());
        if (group != null) {
            // ensure this group is not a duplicate in an app or globally
            if ((group.getApp() == null && raw.getApp() == null)
                    || (group.getApp().getUuid().equals(raw.getApp().getUuid()))) {
                return JsonErrorResponse.conflict("This role already exists"
                        + (group.getApp() == null ? "" : " in this app"));
            }
        }

        group = groupService.save(raw);
        if (group == null) {
            return JsonErrorResponse.internalError("Failed to store group");
        }

        logger.debug("Created group {} [id={}]", group.getName(), group.getId());
        return ResponseEntity.ok(group);
    }

    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    @RequestMapping(value = {"/{groupId}"}, method = RequestMethod.DELETE)
    @ApiOperation(
            value = "Delete a group",
            notes = "",
            code = 202,
            nickname = "deleteGroup"
    )
    public ResponseEntity<Group> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long groupId
    ) {
        
        groupService.delete(groupId);

        logger.debug("Deleted group {}", groupId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

}
