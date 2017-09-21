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
package org.createnet.raptor.common.authentication;

import java.io.Serializable;
import org.createnet.raptor.common.client.ApiClientService;
import org.createnet.raptor.models.auth.request.AuthorizationResponse;
import org.createnet.raptor.models.objects.Device;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class AclPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    ApiClientService api;

    @Override
    public boolean hasPermission(Authentication auth, Object deviceObject, Object permission) {

        if ((auth == null) || !(permission instanceof String)) {
            return false;
        }
        
        String deviceId = null;

        if(deviceObject != null) {
            deviceId = deviceObject.toString();

            if (deviceObject instanceof ResponseEntity) {
                ResponseEntity entity = (ResponseEntity) deviceObject;
                deviceObject = (Device) entity.getBody();
            }

            if (deviceObject instanceof Device) {
                Device dev = (Device) deviceObject;
                deviceId = dev.id();
            }
        }

        return isAuthorized(auth, deviceId, permission.toString().toLowerCase());
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        if ((auth == null) || (targetType == null) || !(permission instanceof String)) {
            return false;
        }
        return isAuthorized(auth, targetId.toString(), permission.toString().toLowerCase());
    }

    private boolean isAuthorized(Authentication auth, String deviceId, String permission) {

        LoginAuthenticationToken tokenAuthentication = (LoginAuthenticationToken) auth;

        if (tokenAuthentication.getUser() == null) {
            return false;
        }

        try {
            AuthorizationResponse response = api.Admin().User().isAuthorized(deviceId, tokenAuthentication.getUser().getUuid(), permission);
            return response.result;
        } catch (Exception ex) {
            return false;
        }

    }

}
