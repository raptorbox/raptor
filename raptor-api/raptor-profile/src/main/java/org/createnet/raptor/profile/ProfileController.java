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
package org.createnet.raptor.profile;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.response.JsonErrorResponse;
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
@RequestMapping(value = "/profile")
@RestController
@ApiResponses(value = {
    @ApiResponse(code = 200, message = "Ok")
    ,@ApiResponse(code = 401, message = "Not authorized")
    ,@ApiResponse(code = 403, message = "Forbidden")
    ,@ApiResponse(code = 500, message = "Internal error")
})
@Api(tags = {"Profile"})
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @RequestMapping(method = RequestMethod.GET, value = "/{userId}")
    @ApiOperation(
            value = "Return a profile value by key",
            notes = "",
            response = org.createnet.raptor.models.profile.Profile.class,
            nickname = "getUserProfile"
    )
    @PreAuthorize("@raptorSecurity.can(principal, 'profile', 'read', #userId)")
    public ResponseEntity<?> getUserProfile(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("userId") String userId
    ) {

        if (userId == null || userId.isEmpty()) {
            return JsonErrorResponse.badRequest();
        }

        List<org.createnet.raptor.models.profile.Profile> prefs = profileService.list(userId);
        return ResponseEntity.ok(prefs.stream().map(p -> toJSON(p.getValue())).collect(Collectors.toList()));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{userId}/{name}")
    @ApiOperation(
            value = "Return a profile value by key",
            notes = "",
            response = org.createnet.raptor.models.profile.Profile.class,
            nickname = "getProfile"
    )
    @PreAuthorize("@raptorSecurity.can(principal, 'profile', 'read', #userId)")
    public ResponseEntity<?> getProfile(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("userId") String userId,
            @PathVariable("name") String name
    ) {

        if ((userId == null || name == null) || (userId.isEmpty() || name.isEmpty())) {
            return JsonErrorResponse.badRequest();
        }

        org.createnet.raptor.models.profile.Profile pref = profileService.get(userId, name);
        if (pref == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "Not found");
        }
        return ResponseEntity.ok(toJSON(pref.getValue()));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{userId}/{name}")
    @ApiOperation(
            value = "Set an profile value by key",
            notes = "",
            response = org.createnet.raptor.models.profile.Profile.class,
            nickname = "setProfile"
    )
    @PreAuthorize("@raptorSecurity.can(principal, 'profile', 'create', #userId) or @raptorSecurity.can(principal, 'profile', 'update', #userId)")
    public ResponseEntity<?> setProfile(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("userId") String userId,
            @PathVariable("name") String name,
            @RequestBody JsonNode body
    ) {

        if ((userId == null || name == null) || (userId.isEmpty() || name.isEmpty())) {
            return JsonErrorResponse.badRequest();
        }

        org.createnet.raptor.models.profile.Profile pref = new org.createnet.raptor.models.profile.Profile(userId, name, body.toString());
        profileService.save(pref);
        return ResponseEntity.ok(toJSON(pref.getValue()));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{userId}/{name}")
    @ApiOperation(
            value = "Drop an profile value by key",
            notes = "",
            response = org.createnet.raptor.models.profile.Profile.class,
            nickname = "deleteProfile"
    )
    @PreAuthorize("@raptorSecurity.can(principal, 'profile', 'delete', #userId)")
    public ResponseEntity<?> deleteProfile(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("userId") String userId,
            @PathVariable("name") String name
    ) {

        if ((userId == null || name == null) || (userId.isEmpty() || name.isEmpty())) {
            return JsonErrorResponse.badRequest();
        }

        org.createnet.raptor.models.profile.Profile pref = profileService.get(userId, name);
        if (pref == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "Not found");
        }

        profileService.delete(pref);
        return ResponseEntity.accepted().build();
    }

    private JsonNode toJSON(String value) {
        try {
            return Device.getMapper().readTree(value);
        } catch (IOException ex) {

        }
        return null;
    }

}
