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
import org.createnet.raptor.auth.service.oauth2.OAuth2ClientDetailService;
import org.createnet.raptor.auth.service.objects.JsonErrorResponse;
import org.createnet.raptor.auth.service.services.UserService;
import org.createnet.raptor.models.auth.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
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
@Api(tags = {"OAuth2Client"})
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
public class OAuthClientController {

    private final Logger logger = LoggerFactory.getLogger(OAuthClientController.class);

    @Autowired
    private OAuth2ClientDetailService clientService;

    @Autowired
    private UserService userService;

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/client", method = RequestMethod.POST)
    @ApiOperation(
            value = "Create an oauth2 client",
            notes = "",
            response = BaseClientDetails.class,
            nickname = "createClient"
    )
    public ResponseEntity<?> create(
            @AuthenticationPrincipal User currentUser,
            @RequestBody BaseClientDetails rawClientDetails
    ) {

        if (rawClientDetails.getClientId() == null || rawClientDetails.getClientId().isEmpty()) {
            return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST, "client_id is missing");
        }
        if (rawClientDetails.getClientSecret() == null || rawClientDetails.getClientSecret().isEmpty()) {
            return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST, "client_secret is missing");
        }

        ClientDetails storedClient = clientService.loadClientByClientId(rawClientDetails.getClientId());

        BaseClientDetails clientDetails = new BaseClientDetails(rawClientDetails);
        if (storedClient != null) {
            return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST, "client_id already exists");
        }

        clientService.addClientDetails(clientDetails);

        logger.debug("User {} created new client {}", currentUser.getUuid(), clientDetails.getClientId());

        return ResponseEntity.status(HttpStatus.CREATED).body(clientDetails);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/client", method = RequestMethod.PUT)
    @ApiOperation(
            value = "Update an oauth2 client",
            notes = "",
            response = BaseClientDetails.class,
            nickname = "updateClient"
    )
    public ResponseEntity<?> update(
            @AuthenticationPrincipal User currentUser,
            @RequestBody BaseClientDetails rawClientDetails
    ) {

        if (rawClientDetails.getClientId() == null || rawClientDetails.getClientId().isEmpty()) {
            return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST, "client_id is missing");
        }
        if (rawClientDetails.getClientSecret() == null || rawClientDetails.getClientSecret().isEmpty()) {
            return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST, "client_secret is missing");
        }

        ClientDetails storedClient = clientService.loadClientByClientId(rawClientDetails.getClientId());

        BaseClientDetails clientDetails = new BaseClientDetails(rawClientDetails);
        if (storedClient == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "client_id already exists");
        }

        clientService.updateClientDetails(clientDetails);

        logger.debug("User {} updated client {}", currentUser.getUuid(), clientDetails.getClientId());

        return ResponseEntity.status(HttpStatus.OK).body(clientDetails);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/client/{clientId}", method = RequestMethod.GET)
    @ApiOperation(
            value = "Get an oauth2 client",
            notes = "",
            response = BaseClientDetails.class,
            nickname = "getClient"
    )
    public ResponseEntity<?> load(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("clientId") String clientId
    ) {

        ClientDetails storedClient = clientService.loadClientByClientId(clientId);

        if (storedClient == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "client not found");
        }

        logger.debug("User {} updated client {}", currentUser.getUuid(), storedClient.getClientId());

        return ResponseEntity.status(HttpStatus.OK).body(storedClient);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/client", method = RequestMethod.GET)
    @ApiOperation(
            value = "Create a list of available oauth2 client",
            notes = "",
            response = BaseClientDetails.class,
            nickname = "listClients"
    )
    public ResponseEntity<?> list(
            @AuthenticationPrincipal User currentUser
    ) {

        List<ClientDetails> clients = clientService.listClientDetails();

        logger.debug("Loaded {} clients", clients.size());

        return ResponseEntity.status(HttpStatus.OK).body(clients);
    }

}
