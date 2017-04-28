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
package org.createnet.raptor.api.common.authentication;

import java.io.Serializable;
import org.createnet.raptor.models.acl.Permissions;
import org.createnet.raptor.models.auth.request.AuthorizationResponse;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.sdk.Raptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class AclPermissionEvaluator implements PermissionEvaluator {

    @Value("${raptor.auth.url}")
    private String authUrl;

    @Override
    public boolean hasPermission(Authentication auth, Object deviceObject, Object permission) {

        if ((auth == null) || (deviceObject == null) || !(permission instanceof String)) {
            return false;
        }

        String deviceId = deviceObject.toString();

        if(deviceObject instanceof ResponseEntity) {
            ResponseEntity entity = (ResponseEntity) deviceObject;
            deviceObject = (Device)entity.getBody();
        }
        
        if(deviceObject instanceof Device) {
            Device dev = (Device)deviceObject;
            deviceId = dev.getId();
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
            Permissions p = Permissions.valueOf(permission);
            deviceId = (p == Permissions.create || p == Permissions.list) ? null : deviceId;
            Raptor r = new Raptor(authUrl, tokenAuthentication.getToken());
            AuthorizationResponse response = r.Admin().User().isAuthorized(deviceId, tokenAuthentication.getUser().getUuid(), p);
            return response.result;
        } catch (Exception ex) {
            return false;
        }

    }

}
