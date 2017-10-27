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
import org.createnet.raptor.common.configuration.TokenHelper;
import org.createnet.raptor.models.auth.request.AuthorizationRequest;
import org.createnet.raptor.models.auth.request.AuthorizationResponse;
import org.createnet.raptor.models.auth.request.SyncRequest;
import org.createnet.raptor.models.acl.permission.RaptorPermission;
import org.createnet.raptor.models.response.JsonErrorResponse;
import org.createnet.raptor.models.auth.AclDevice;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.auth.services.AclDeviceService;
import org.createnet.raptor.auth.services.AclTokenService;
import org.createnet.raptor.auth.services.AuthAppService;
import org.createnet.raptor.auth.services.AuthDeviceService;
import org.createnet.raptor.auth.services.AuthTreeService;
import org.createnet.raptor.auth.services.TokenService;
import org.createnet.raptor.auth.services.UserService;
import org.createnet.raptor.common.authentication.RaptorSecurity;
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
@RequestMapping(value = "/auth")
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
public class AclController {

    private static final Logger logger = LoggerFactory.getLogger(AclController.class);

    @Autowired
    private AuthDeviceService deviceService;

    @Autowired
    private AuthAppService appService;

    @Autowired
    private AuthTreeService treeService;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TokenHelper tokenHelper;

    @Autowired
    private AclDeviceService aclDeviceService;

    @Autowired
    private AclTokenService aclTokenService;
    
    @Autowired
    private RaptorSecurity raptorSecurity;

    
    
    @RequestMapping(value = "/check", method = RequestMethod.POST)
    @ApiOperation(
            value = "Check user permission on a device",
            notes = "",
            response = AuthorizationResponse.class,
            nickname = "checkPermission"
    )
    public ResponseEntity<?> checkPermission(
            @AuthenticationPrincipal User currentUser,
            @RequestBody AuthorizationRequest body,
            @RequestHeader("${raptor.auth.header}") String rawToken
    ) {

        AuthorizationResponse response = new AuthorizationResponse();

        User user = currentUser;
        if (body.userId != null) {
            user = userService.getByUuid(body.userId);
        }

        logger.debug("Check if user {} can `{}` on {} {}", user.getUuid(), body.permission, body.type, body.objectId);

        Permission permission = RaptorPermission.fromLabel(body.permission);
        if (permission == null) {
            logger.warn("Permission not found for user {} `{}` on object {}", user.getUuid(), body.permission, body.objectId);
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND);
        }

        if (body.objectId == null && (permission == RaptorPermission.CREATE)) {
            // set true here, token permission will check over permission without objectId
            response.result = true;
        } else {

            AclDevice device = deviceService.getByUuid(body.objectId);
            if (device == null) {
                return JsonErrorResponse.entity(HttpStatus.NOT_FOUND);
            }

            response.result = aclDeviceService.check(device, user, permission);
        }

        // check for token specific permission if user level ACL are ok
        if (response.result) {
            // check token level ACL
            Token token = tokenService.read(tokenHelper.extractToken(rawToken));
            if (!aclTokenService.list(token, user).isEmpty()) {
                response.result = aclTokenService.check(token, user, permission);
            }
        }

        logger.debug("Device permission check result: [deviceId:{} permission:{} result:{}]", body.objectId, body.permission, response.result);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @RequestMapping(value = "/sync", method = RequestMethod.POST)
    @ApiOperation(
            value = "Sync permission on an entity",
            notes = "",
            code = 202,
            nickname = "syncPermission"
    )
    public ResponseEntity<?> syncPermission(
            @AuthenticationPrincipal User currentUser,
            @RequestBody SyncRequest body
    ) {

        switch (body.type) {
            case app:
                appService.sync(currentUser, body);
                break;
            case device:
                deviceService.sync(currentUser, body);
                break;
            case tree:
                treeService.sync(currentUser, body);
                break;
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

}
