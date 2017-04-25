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
import java.util.List;
import java.util.stream.Collectors;
import org.createnet.raptor.auth.service.services.RaptorUserDetailsService;
import org.createnet.raptor.auth.service.acl.RaptorPermission;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.auth.service.exception.PermissionNotFoundException;
import org.createnet.raptor.models.response.JsonErrorResponse;
import org.createnet.raptor.models.auth.request.PermissionRequestBatch;
import org.createnet.raptor.auth.service.services.AclTokenService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
public class TokenPermissionController {

    private static final Logger logger = LoggerFactory.getLogger(TokenPermissionController.class);

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private AclTokenService aclTokenService;

    @RequestMapping(value = "/permission/token/{tokenId}/{userUuid}", method = RequestMethod.GET)
    @ApiOperation(
            value = "List user permissions on a token",
            notes = "",
            response = String.class,
            responseContainer = "List",
            nickname = "getUserPermissions"
    )
    public ResponseEntity<?> listPermissions(
            @PathVariable("tokenId") Long tokenId,
            @PathVariable("userUuid") String userUuid,
            @AuthenticationPrincipal User currentUser
    ) {

        Token token = tokenService.read(tokenId);
        if (token == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "Token not found");
        }

        User user = userService.getByUuid(userUuid);
        if (user == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "User not found");
        }

        //todo: add acl checks
        if (!user.getUuid().equals(currentUser.getUuid()) || !currentUser.isAdmin()) {
            return JsonErrorResponse.entity(HttpStatus.UNAUTHORIZED);
        }

        List<String> permissions = RaptorPermission.toLabel(aclTokenService.list(token, user));
        return ResponseEntity.status(HttpStatus.OK).body(permissions);
    }

    @RequestMapping(value = "/permission/token/{tokenId}", method = RequestMethod.GET)
    @ApiOperation(
            value = "List current user permissions on a token",
            notes = "",
            response = String.class,
            responseContainer = "List",
            nickname = "getPermissions"
    )
    public ResponseEntity<?> listOwnPermissions(
            @PathVariable("tokenId") Long tokenId,
            @AuthenticationPrincipal User currentUser
    ) {

        Token token = tokenService.read(tokenId);
        if (token == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "Token not found");
        }

        User user = userService.getByUuid(currentUser.getUuid());
        if (user == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "User not found");
        }

        List<String> permissions = RaptorPermission.toLabel(aclTokenService.list(token, user));
        return ResponseEntity.status(HttpStatus.OK).body(permissions);
    }

    @RequestMapping(value = "/permission/token/{tokenId}", method = RequestMethod.PUT)
    @ApiOperation(
            value = "Save user permissions on a token",
            notes = "",
            response = String.class,
            responseContainer = "List",
            nickname = "setPermissions"
    )
    public ResponseEntity<?> setPermission(
            @RequestBody PermissionRequestBatch body,
            @PathVariable("tokenId") Long tokenId,
            @AuthenticationPrincipal User currentUser
    ) {

        Token token = tokenService.read(tokenId);
        if (token == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "Token not found");
        }

        User user = currentUser;
        if (body.user != null) {
            user = userService.getByUuid(body.user);
            if (user == null) {
                return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "User not found");
            }

            //todo: add acl checks
            if (!user.getUuid().equals(currentUser.getUuid()) || !currentUser.isAdmin()) {
                return JsonErrorResponse.entity(HttpStatus.UNAUTHORIZED);
            }

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

        aclTokenService.set(token, user, permissions);
        List<String> settedPermissions = RaptorPermission.toLabel(aclTokenService.list(token, user));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(settedPermissions);
    }

}
