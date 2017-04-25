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
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.createnet.raptor.profile.UserPreferencesService;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.profile.UserPreference;
import org.createnet.raptor.models.response.JsonErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class UserPreferencesController {

    @Autowired
    private UserPreferencesService preferences;

    @RequestMapping(method = RequestMethod.GET, value = "/{userId}")
    @ApiOperation(
            value = "Return all the user preferences",
            notes = "",
            response = UserPreference.class,
            nickname = "getPreferences"
    )
    public ResponseEntity<?> getPreferences(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("userId") String userId
    ) {
        List<UserPreference> prefs = preferences.list(userId);
        return ResponseEntity.ok(prefs.stream().map(p -> toJSON(p.getValue())).collect(Collectors.toList()));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{userId}/{name}")
    @ApiOperation(
            value = "Return a user preference by name",
            notes = "",
            response = UserPreference.class,
            nickname = "getPreference"
    )
    public ResponseEntity<?> getPreference(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("userId") String userId,
            @PathVariable("name") String name
    ) {
        UserPreference pref = preferences.get(userId, name);
        if (pref == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "Not found");
        }
        return ResponseEntity.ok(toJSON(pref.getValue()));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{userId}/{name}")
    @ApiOperation(
            value = "Set an user preference by name",
            notes = "",
            response = UserPreference.class,
            nickname = "setPreference"
    )
    public ResponseEntity<?> setPreference(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("userId") String userId,
            @PathVariable("name") String name,
            @RequestBody JsonNode body
    ) {
        UserPreference pref = new UserPreference(userId, name, body.toString());
        preferences.save(pref);
        return ResponseEntity.ok(toJSON(pref.getValue()));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{userId}/{name}")
    @ApiOperation(
            value = "Drop an user preference by name",
            notes = "",
            response = UserPreference.class,
            nickname = "deletePreference"
    )
    public ResponseEntity<?> deletePreference(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("userId") String userId,
            @PathVariable("name") String name
    ) {

        UserPreference pref = preferences.get(userId, name);
        if (pref == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "Not found");
        }

        preferences.delete(pref);
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
