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

import org.createnet.raptor.auth.service.objects.JsonErrorResponse;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.auth.service.services.TokenService;
import org.createnet.raptor.auth.service.services.UserService;
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
@RestController
public class TokenController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserService userService;

    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/token/{uuid}")
    public ResponseEntity<?> getTokens(
            @AuthenticationPrincipal User user,
            @PathVariable String uuid
    ) {
        // TODO add ACL checks
        if(!user.getUuid().equals(uuid) && !user.isSuperAdmin()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JsonErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Not authorized"));
        }

        return ResponseEntity.ok(tokenService.list(uuid));
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/token")
    public Iterable<Token> getUserTokens(
            @AuthenticationPrincipal User user
    ) {
        return tokenService.list(user.getUuid());
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/token/{tokenId}", method = RequestMethod.GET)
    public ResponseEntity<?> get(
            @AuthenticationPrincipal User user,
            @PathVariable Long tokenId
    ) {

        Token token = tokenService.read(tokenId);

        // TODO add ACL checks
        if (user.getId().longValue() != token.getUser().getId().longValue()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JsonErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Not authorized"));
        }

        return ResponseEntity.ok(token);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/token/{tokenId}", method = RequestMethod.PUT)
    public ResponseEntity<?> update(
            @AuthenticationPrincipal User user,
            @PathVariable Long tokenId,
            @RequestBody Token token
    ) {

        // TODO add ACL checks        
        if (user.getId().longValue() != token.getUser().getId().longValue()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JsonErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Not authorized"));
        }

        if (token.getSecret().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JsonErrorResponse(400, "Secret cannot be empty"));
        }

        token.setId(tokenId);

        // Generate the JWT token
        tokenService.generateToken(token);

        return ResponseEntity.status(HttpStatus.OK).body(tokenService.update(token));
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/token", method = RequestMethod.POST)
    public ResponseEntity<?> create(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Token rawToken
    ) {

        if (rawToken.getSecret().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JsonErrorResponse(400, "Secret cannot be empty"));
        }

        Token token = new Token();
        token.setName(rawToken.getName());
        token.setEnabled(rawToken.getEnabled());
        token.setExpires(rawToken.getExpires());
        token.setSecret(rawToken.getSecret());
        token.setType(rawToken.getType());
        token.setUser(currentUser);

        // Generate the JWT token
        tokenService.generateToken(token);

        Token token2 = tokenService.create(token);

        if (token2 == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new JsonErrorResponse(500, "Cannot create the token"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(token2);
    }

}
