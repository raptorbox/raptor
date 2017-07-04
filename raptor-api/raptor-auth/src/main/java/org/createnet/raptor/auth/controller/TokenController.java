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
import java.util.ArrayList;
import java.util.List;
import org.createnet.raptor.models.response.JsonErrorResponse;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.auth.services.TokenService;
import org.createnet.raptor.auth.services.UserService;
import org.createnet.raptor.models.apidocs.ApiDocsToken;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@RestController
@Api(tags = {"Token"})
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
public class TokenController {

    private final Logger logger = LoggerFactory.getLogger(TokenController.class);

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserService userService;

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/token", method = RequestMethod.GET)
    @ApiOperation(
            value = "List tokens",
            notes = "",
            response = ApiDocsToken.class,
            responseContainer = "Iterable",
            nickname = "getTokens"
    )
    public ResponseEntity<?> getTokens(
            @RequestParam(value = "uuid", required = false) String uuid,
            @AuthenticationPrincipal User user
    ) {

        if (uuid == null || uuid.isEmpty()) {
            uuid = user.getUuid();
        } else {
            User reqUser = userService.getByUuid(uuid);
            if (reqUser == null) {
                return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "User not found");
            }
        }

        // @TODO: add ACL checks. Currently users can list their tokens or must be SuperAdmin
        if (!user.getUuid().equals(uuid) && !user.isSuperAdmin()) {
            return JsonErrorResponse.entity(HttpStatus.UNAUTHORIZED);
        }
        
        Iterable<Token> tokens = tokenService.list(uuid);
        final List<Token> userToken = new ArrayList();
        
        tokens.forEach((Token token) -> {
            if(token.getType().equals(Token.Type.DEFAULT)) {
                userToken.add(token);
            }
        });
        
        logger.debug("Found {} tokens", userToken.size());
        
        return ResponseEntity.ok(userToken);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/token/{tokenId}", method = RequestMethod.GET)
    @ApiOperation(
            value = "Get a token by ID",
            notes = "",
            response = ApiDocsToken.class,
            nickname = "getToken"
    )
    public ResponseEntity<?> getToken(
            @AuthenticationPrincipal User user,
            @PathVariable Long tokenId
    ) {

        Token token = tokenService.read(tokenId);

        // TODO add ACL checks
        if (user.getId().longValue() != token.getUser().getId().longValue()) {
            return JsonErrorResponse.entity(HttpStatus.UNAUTHORIZED);
        }

        if (token.isLoginToken()) {
            return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST, "Can not access this token");
        }

        return ResponseEntity.ok(token);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/token", method = RequestMethod.POST)
    @ApiOperation(
            value = "Create a token",
            notes = "",
            response = ApiDocsToken.class,
            nickname = "createToken"
    )
    public ResponseEntity<?> createToken(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Token rawToken
    ) {

        if (rawToken.getSecret() == null || rawToken.getSecret().isEmpty()) {
            return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST, "Secret cannot be empty");
        }

        Token token = new Token(rawToken);
        token.setUser(currentUser);
        token.setType(Token.Type.DEFAULT);

        // Generate the token based on provided secret
        tokenService.generateToken(token);
        Token token2 = tokenService.save(token);

        if (token2 == null) {
            return JsonErrorResponse.entity(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot create the token");
        }

        logger.debug("User {} created new token {} {}", currentUser.getUuid(), token2.getName(),  token2.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(token2);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/token/{tokenId}", method = RequestMethod.PUT)
    @ApiOperation(
            value = "Update a token",
            notes = "",
            response = ApiDocsToken.class,
            nickname = "updateToken"
    )
    public ResponseEntity<?> updateToken(
            @AuthenticationPrincipal User user,
            @PathVariable Long tokenId,
            @RequestBody Token rawToken
    ) {

        Token token = tokenService.read(tokenId);
        
        if(token == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "Token not found");
        }
        
        // TODO add ACL checks        
        if (token.getUser() == null || user.getId().longValue() != token.getUser().getId().longValue()) {
            return JsonErrorResponse.entity(HttpStatus.UNAUTHORIZED);
        }

        // Disallow update of LOGIN tokens
        if (token.getType() == Token.Type.LOGIN) {
            return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST, "Login token cannot be modified");
        }

        token.merge(rawToken);
        token.setUser(user);
        token.setType(Token.Type.DEFAULT);

        if (token.getSecret() == null || token.getSecret().isEmpty()) {
            return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST, "Secret cannot be empty");
        }

        // Generate the JWT token
        tokenService.generateToken(token);

        Token token2 = tokenService.save(token);

        logger.debug("User {} update token {}", user.getUuid(), token2.getId());

        return ResponseEntity.status(HttpStatus.OK).body(token2);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/token/{tokenId}", method = RequestMethod.DELETE)
    @ApiOperation(
            value = "Delete a token",
            notes = "",
            response = ApiDocsToken.class,
            nickname = "deleteToken"
    )
    public ResponseEntity<?> deleteToken(
            @AuthenticationPrincipal User user,
            @PathVariable Long tokenId
    ) {

        Token token = tokenService.read(tokenId);

        if(token == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "Token not found");
        }
        
        // TODO add ACL checks        
        if (token.getUser() == null || user.getId().longValue() != token.getUser().getId().longValue()) {
            return JsonErrorResponse.entity(HttpStatus.UNAUTHORIZED);
        }

        tokenService.delete(token);

        logger.debug("User {} deleted token {}", user.getUuid(), token.getId());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

}
