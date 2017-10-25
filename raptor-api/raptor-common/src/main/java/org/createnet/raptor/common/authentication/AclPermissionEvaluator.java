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
import org.createnet.raptor.models.acl.Operation;
import org.createnet.raptor.models.auth.request.AuthorizationResponse;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.response.JsonError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class AclPermissionEvaluator implements PermissionEvaluator {
    
    Logger log = LoggerFactory.getLogger(AclPermissionEvaluator.class);
    
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
                if (entity.getBody() instanceof JsonError) {
                    LoginAuthenticationToken tokenAuthentication = (LoginAuthenticationToken) auth;
                    JsonError err = (JsonError) entity.getBody();
                    log.warn("Got error response for [user={} permission={}] {}", tokenAuthentication.getUser().getUsername(), permission, err.message);
                    return false;
                }
                deviceObject = (Device) entity.getBody();
            }

            if (deviceObject instanceof Device) {
                Device dev = (Device) deviceObject;
                deviceId = dev.id();
            }
        }
        
        log.debug("Check authorization for user={} device={} permission={}", auth.getPrincipal(), deviceId, permission.toString().toLowerCase());
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
            Operation p = Operation.valueOf(permission);
            log.debug("Check authorization for user={} device={} permission={}", tokenAuthentication.getUser().getUuid(), deviceId, p);
            AuthorizationResponse response = api.Admin().User().isAuthorized(deviceId, tokenAuthentication.getUser().getUuid(), p);
            return response.result;
        } catch (Exception ex) {
            return false;
        }

    }

}
