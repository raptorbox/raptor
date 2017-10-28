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
import org.createnet.raptor.common.client.InternalApiClientService;
import org.createnet.raptor.models.acl.EntityType;
import org.createnet.raptor.models.acl.Operation;
import org.createnet.raptor.models.acl.Owneable;
import org.createnet.raptor.models.auth.Permission;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.auth.request.AuthorizationResponse;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.response.JsonError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class RaptorSecurity {

    @Autowired
    InternalApiClientService api;

    Logger log = LoggerFactory.getLogger(RaptorSecurity.class);

    public boolean list(User u, EntityType entity) {
        
        boolean hasPermission = can(u, entity, Operation.read);
        if (hasPermission) {
            return true;
        }
        
        Permission p = new Permission(entity, Operation.read, true);
        hasPermission = u.hasPermission(p);
        log.debug("`{}` can {} list `{}`", u.getUsername(), hasPermission ? "" : "NOT", entity);

        return hasPermission;
    }
    
    public boolean can(User u, EntityType entity, Operation operation) {
        return can(u, entity, operation, null);
    }

    /**
     * Check if an user has the permission to operate on an object (if
     * available) or on an entity type (like users, apps, devices or trees).
     *
     * @param u
     * @param entity
     * @param operation
     * @param obj
     * @return
     */
    public boolean can(User u, EntityType entity, Operation operation, Object obj) {
        
        log.debug("Check if user `{}` can `{}` on  `{}` [id={}]", u.getUsername(), operation, entity, obj);
        
        // is an admin
        if (u.isAdmin()) {
            log.debug("`{}` is admin", u.getUsername());
            return true;
        }

        boolean hasPermission = false;

        // check if the current user own the subject        
        hasPermission = isOwner(u, entity, operation, obj);
        if (hasPermission) {
            log.debug("`{}` is owner", u.getUsername());
            return true;
        }

        // can admin the entity type eg. device_admin
        Permission p = new Permission(entity, Operation.admin);
        hasPermission = u.hasPermission(p);
        if (hasPermission) {
            log.debug("`{}` can `admin` on {}", u.getUsername(), entity);
            return true;
        }

        // did the check beore
        if (operation != Operation.admin) {
            // has specific permission on an entity type eg. device_read
            p = new Permission(entity, operation);
            hasPermission = u.hasPermission(p);
            if(hasPermission) {
                log.debug("`{}` can `{}` on `{}`", u.getUsername(), operation, entity);
                return true;
            }
        }

        log.debug("User `{}` NOT ALLOWED to `{}` on `{}` [id={}]", u.getUsername(), operation, entity, obj);

        return false;
    }

    public boolean isOwner(User u, EntityType entity, Operation operation, Object obj) {

        if (obj == null) {
            return false;
        }

        // has specific permission on own entity type eg. device_read_own
        Permission p = new Permission(entity, operation, true);
        boolean hasPermission = u.hasPermission(p);
        if (hasPermission) {

            String ownerId = null;

            if (obj instanceof String || obj instanceof Long) {
                try {
                    obj = loadById(entity, obj);
                } catch(Exception ex) {
                    log.error("Failed to load `ownerId`: {}", ex.getMessage(), ex);
                    return false;
                }
            }

            if (obj instanceof Owneable) {
                ownerId = ((Owneable) obj).getOwnerId();
            }

            if (ownerId == null) {
                return false;
            }

            return ownerId.equals(u.getUuid());
        }

        return false;
    }

    public boolean hasPermission(Authentication auth, Object deviceObject, Object permission) {

        if ((auth == null) || !(permission instanceof String)) {
            return false;
        }

        String deviceId = null;

        if (deviceObject != null) {
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
            AuthorizationResponse response = api.Admin().User().isAuthorized(EntityType.device, deviceId, tokenAuthentication.getUser().getUuid(), p);
            return response.result;
        } catch (Exception ex) {
            return false;
        }

    }

    private Owneable loadById(EntityType entity, Object uuid) {

        if (uuid == null) {
            return null;
        }

        switch (entity) {
            case device:
                return api.Inventory().load((String) uuid);
            case app:
                return api.App().load((String) uuid);
            case tree:
                return api.Tree().tree((String) uuid);
            case user:
            case profile:
                return api.Admin().User().get((String) uuid);
            case token:
                return api.Admin().Token().read((Long) uuid);
            default:
                return null;
        }
    }

}
