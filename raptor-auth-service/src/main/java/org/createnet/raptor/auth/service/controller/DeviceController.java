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
package org.createnet.raptor.auth.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.stream.Collectors;
import org.createnet.raptor.auth.entity.AuthorizationRequest;
import org.createnet.raptor.auth.entity.AuthorizationResponse;
import org.createnet.raptor.auth.entity.SyncRequest;
import org.createnet.raptor.auth.service.RaptorUserDetailsService;
import org.createnet.raptor.auth.service.acl.RaptorPermission;
import org.createnet.raptor.auth.service.objects.JsonErrorResponse;
import org.createnet.raptor.models.auth.Device;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.auth.service.services.AclDeviceService;
import org.createnet.raptor.auth.service.services.AclTokenService;
import org.createnet.raptor.auth.service.services.DeviceService;
import org.createnet.raptor.auth.service.services.TokenService;
import org.createnet.raptor.auth.service.services.UserService;
import org.createnet.raptor.models.auth.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@RestController
@PreAuthorize("isAuthenticated()")
@Api(tags = {"User", "Permission"})
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
public class DeviceController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceController.class);

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private AclDeviceService aclDeviceService;
    
    @Autowired
    private AclTokenService aclTokenService;

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    @ApiOperation(
            value = "Check user permission on a device",
            notes = "",
            response = AuthorizationResponse.class,
            nickname = "checkPermission"
    )
    public ResponseEntity<?> checkPermission(
            @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails currentUser,
            @RequestBody AuthorizationRequest body,
            @RequestHeader("Authorization") String rawToken
    ) {

        AuthorizationResponse response = new AuthorizationResponse();

        switch (body.getOperation()) {
            case User:

                response.result = true;
                response.userId = currentUser.getUuid();
                response.roles = currentUser.getRoles()
                        .stream()
                        .map((r) -> r.getName())
                        .collect(Collectors.toList());

                break;
            case Permission:

                User user = (User) currentUser;
                if (body.userId != null) {
                    user = userService.getByUuid(body.userId);
                }

                logger.debug("Check if user {} can `{}` on object {}", user.getUuid(), body.permission, body.objectId);

                Permission permission = RaptorPermission.fromLabel(body.permission);
                if (permission == null) {
                    logger.warn("Permission not found for user {} `{}` on object {}", user.getUuid(), body.permission, body.objectId);
                    return JsonErrorResponse.entity(HttpStatus.NOT_FOUND);
                }

                /**
                 * @TODO CREATE permission should be attached to an user rather
                 * than on the object itself
                 */
                final boolean isCreate = (body.objectId == null && permission == RaptorPermission.CREATE);
                if (isCreate) {
                    response.result = true;
                } else {

                    Device device = null;
                    if (permission != RaptorPermission.LIST) {
                        device = deviceService.getByUuid(body.objectId);
                        if (device == null) {
                            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND);
                        }
                    }

                    response.result = aclDeviceService.check(device, user, permission);
                    
                    if (response.result) {
                        // check token level ACL
                        Token token = tokenService.read(rawToken);
                        aclTokenService.check(token, user, permission);
                    }

                }

                break;
            default:

                response.result = false;
                break;
        }

        logger.debug("Check request result: {}", response.result);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @RequestMapping(value = "/sync", method = RequestMethod.POST)
    @ApiOperation(
            value = "Sync user permission on a device",
            notes = "",
            code = 202,
            nickname = "syncPermission"
    )    
    public ResponseEntity<?> syncPermission(
            @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails currentUser,
            @RequestBody SyncRequest body
    ) {

        deviceService.sync(currentUser, body);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

}
