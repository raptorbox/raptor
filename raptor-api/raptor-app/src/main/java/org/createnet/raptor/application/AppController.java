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

import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Arrays;
import org.createnet.raptor.common.client.ApiClientService;
import org.createnet.raptor.common.query.AppQueryBuilder;
import org.createnet.raptor.models.acl.Operation;
import org.createnet.raptor.models.app.App;
import org.createnet.raptor.models.app.AppGroup;
import org.createnet.raptor.models.app.AppUser;
import org.createnet.raptor.models.auth.DefaultGroups;
import org.createnet.raptor.models.auth.StaticGroup;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.exception.RequestException;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.query.AppQuery;
import org.createnet.raptor.models.response.JsonErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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
@RequestMapping(value = "/app")
@RestController
@ApiResponses(value = {
    @ApiResponse(code = 200,message = "Ok")
    ,@ApiResponse(code = 401,message = "Not authorized")
    ,@ApiResponse(code = 403,message = "Forbidden")
    ,@ApiResponse(code = 500,message = "Internal error")
})
@Api(tags = {"Application"})
public class AppController {

    private final Logger log = LoggerFactory.getLogger(AppController.class);

    @Autowired
    private AppService appService;

    @Autowired
    private AppEventPublisher eventPublisher;

    @Autowired
    private ApiClientService raptor;

    /**
     * Register app ACL in the auth API
     *
     * @param op
     * @param app
     * @return
     */
    protected boolean syncACL(Operation op, App app) {
        try {
            raptor.Admin().User().sync(op, app);
        } catch (RequestException ex) {
            log.error("Failed to sync ACL", ex.getMessage());
            return false;
        }
        return true;
    }

    protected void normalizeApp(App app) {

        // ensure admin role is avail
        if (app.getAdminGroup() == null) {
            app.getGroups().add(new AppGroup(DefaultGroups.admin));
        }

        // ensure user role is avail
        if (app.getUserGroup() == null) {
            app.getGroups().add(new AppGroup(DefaultGroups.appUser));
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(
            value = "Return the apps owned by an user",
            notes = "",
            response = org.createnet.raptor.models.app.App.class,
            nickname = "getApps"
    )
    @PreAuthorize("@raptorSecurity.list(principal, 'app')")
    public ResponseEntity<?> getApps(
            @AuthenticationPrincipal User currentUser,
            Pageable pageable
    ) {

        AppQuery query = new AppQuery();
        if (!currentUser.isAdmin()) {
            query.users.in(currentUser.getId());
        }

        AppQueryBuilder qb = new AppQueryBuilder(query);
        Predicate p = qb.getPredicate();
        Iterable<App> apps = appService.find(p, pageable);

        return ResponseEntity.ok(apps);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/search")
    @ApiOperation(
            value = "Search for apps",
            notes = "",
            response = org.createnet.raptor.models.app.App.class,
            nickname = "searchApps"
    )
    @PreAuthorize("@raptorSecurity.list(principal, 'app')")
    public ResponseEntity<?> searchApps(
            @AuthenticationPrincipal User currentUser,
            @RequestBody AppQuery query
    ) {

        if (!currentUser.isAdmin()) {
            query.users.in(currentUser.getId());
        }

        AppQueryBuilder qb = new AppQueryBuilder(query);
        Iterable<App> apps = appService.find(qb.getPredicate(), qb.getPaging());

        return ResponseEntity.ok(apps);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(
            value = "Create a new app",
            notes = "",
            response = org.createnet.raptor.models.app.App.class,
            nickname = "createApp"
    )
    @PreAuthorize("@raptorSecurity.can(principal, 'app', 'create')")
    public ResponseEntity<?> createApp(
            @AuthenticationPrincipal User currentUser,
            @RequestBody App app
    ) {
        
        if (app.getUserId() == null) {
            app.setUserId(currentUser.getId());
        }

        normalizeApp(app);
        
        try {
            appService.validate(app);
        } catch (RaptorComponent.ValidationException ex) {
            return JsonErrorResponse.badRequest(ex.getMessage());
        }

        // ensure owner is also admin in users list
        if (!app.getUsers().contains(new AppUser(app.getUserId()))) {
            app.addUser(currentUser, Arrays.asList(StaticGroup.admin.name()));
        }

        App saved = appService.save(app);
        syncACL(Operation.create, app);
        
        eventPublisher.create(app);

        log.debug("Created app {} ({})", app.getName(), app.getId());
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("@raptorSecurity.can(principal, 'app', 'read', #appId)")
    @RequestMapping(method = RequestMethod.GET, value = "/{appId}")
    @ApiOperation(
            value = "Load an app",
            notes = "",
            response = org.createnet.raptor.models.app.App.class,
            nickname = "readApp"
    )
    public ResponseEntity<?> readApp(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("appId") String appId
    ) {
        App stored = appService.get(appId);
        if (stored == null) {
            return JsonErrorResponse.notFound();
        }
        return ResponseEntity.ok(stored);
    }

    @PreAuthorize("@raptorSecurity.can(principal, 'app', 'update', #appId)")
    @RequestMapping(method = RequestMethod.PUT, value = "/{appId}")
    @ApiOperation(
            value = "Update an app",
            notes = "",
            response = org.createnet.raptor.models.app.App.class,
            nickname = "updateApp"
    )
    public ResponseEntity<?> updateApp(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("appId") String appId,
            @RequestBody App app
    ) {

        App stored = appService.get(appId);
        if (stored == null) {
            return JsonErrorResponse.notFound();
        }

        if (!currentUser.isAdmin() && !currentUser.getId().equals(app.getUserId()) && !app.isAdmin(currentUser)) {
            return JsonErrorResponse.unauthorized();
        }

        app.setId(stored.getId());
        app.setUserId(stored.getUserId());

        normalizeApp(app);

        // ensure owner is also admin in users list
        if (!app.getUsers().contains(new AppUser(app.getUserId()))) {
            app.addUser(currentUser, Arrays.asList(StaticGroup.admin.name()));
        }

        try {
            appService.validate(app);
        } catch (RaptorComponent.ValidationException ex) {
            return JsonErrorResponse.badRequest(ex.getMessage());
        }

        App saved = appService.save(app);
        syncACL(Operation.update, app);

        eventPublisher.update(app);


        log.debug("Updated app {} ({}) by {}", app.getName(), app.getId(), currentUser.getId());
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("@raptorSecurity.can(principal, 'app', 'delete', #appId)")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{appId}")
    @ApiOperation(
            value = "Delete an app",
            notes = "",
            response = org.createnet.raptor.models.app.App.class,
            nickname = "deleteApp"
    )
    public ResponseEntity<?> deleteApp(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("appId") String appId
    ) {

        App app = appService.get(appId);
        if (app == null) {
            return JsonErrorResponse.notFound();
        }

        if (!currentUser.isAdmin() && !currentUser.getId().equals(app.getUserId()) && !app.isAdmin(currentUser)) {
            return JsonErrorResponse.unauthorized();
        }

        appService.delete(app);

        eventPublisher.delete(app);

        syncACL(Operation.delete, app);

        log.debug("Deleted app {} ({})", app.getName(), app.getId());
        return ResponseEntity.accepted().build();
    }

}
