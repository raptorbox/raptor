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
package org.createnet.raptor.application;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import org.createnet.raptor.models.app.App;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.response.JsonErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@RequestMapping(value = "/application")
@RestController
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
@Api(tags = {"Application"})
public class AppController {
    
    private final Logger log = LoggerFactory.getLogger(AppController.class);
    
    @Autowired
    private AppService appService;

    @RequestMapping(method = RequestMethod.GET, value = "/")
    @ApiOperation(
            value = "Return the apps owned by an user",
            notes = "",
            response = org.createnet.raptor.models.app.App.class,
            nickname = "getUserApps"
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserApps(
            @AuthenticationPrincipal User currentUser
    ) {
        List<App> apps = appService.list(currentUser.getUuid());
        log.debug("Found %s apps", apps.size());
        return ResponseEntity.ok(apps);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/")
    @ApiOperation(
            value = "Create a new app",
            notes = "",
            response = org.createnet.raptor.models.app.App.class,
            nickname = "createApp"
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createApp(
            @AuthenticationPrincipal User currentUser,
            @RequestBody App app
    ) {

        try {
            app.validate();
        } catch (RaptorComponent.ValidationException ex) {
            return JsonErrorResponse.badRequest(ex.getMessage());
        }

        App saved = appService.save(app);
        log.debug("Created app %s (%s)", app.getName(), app.getId());
        return ResponseEntity.ok(saved);
    }

}
