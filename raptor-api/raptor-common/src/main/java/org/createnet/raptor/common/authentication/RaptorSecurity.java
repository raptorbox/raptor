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

import org.createnet.raptor.common.client.InternalApiClientService;
import org.createnet.raptor.models.acl.AclDomain;
import org.createnet.raptor.models.acl.EntityType;
import org.createnet.raptor.models.acl.Operation;
import org.createnet.raptor.models.acl.Owneable;
import org.createnet.raptor.models.auth.Permission;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.auth.request.AuthorizationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

        String objectId = null;
        String domain = null;
        if (obj != null) {
            if (obj instanceof String) {
                objectId = (String) obj;
            }
            if (obj instanceof Owneable) {
                objectId = ((Owneable) obj).getId();
            }
            if (obj instanceof AclDomain) {
                domain = ((AclDomain) obj).getDomain();
            }
        }

        if (operation == Operation.create) {
            //do not set an id  on creation
            objectId = null;
        }

        AuthorizationResponse r = api.Admin().User().isAuthorized(u.getId(), entity, operation, objectId, domain);
        return r.result;
    }

}
