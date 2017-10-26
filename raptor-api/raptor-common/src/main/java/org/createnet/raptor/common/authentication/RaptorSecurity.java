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

import org.createnet.raptor.models.acl.EntityType;
import org.createnet.raptor.models.acl.Operation;
import org.createnet.raptor.models.acl.Owneable;
import org.createnet.raptor.models.auth.Permission;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class RaptorSecurity {

    public boolean can(RaptorUserDetails u, EntityType entity, Operation operation) {

        // is an admin
        if (u.isAdmin()) {
            return true;
        }

        // can admin the entity type eg. device_admin
        Permission p = new Permission(entity, Operation.admin);
        boolean hasPermission = u.getAuthorities().contains(p);
        if (hasPermission) {
            return true;
        }

        // has specific permission on an entity type eg. device_read
        p = new Permission(entity, operation);
        hasPermission = u.getAuthorities().contains(p);

        return hasPermission;
    }

    public boolean isOwner(RaptorUserDetails u, Object obj, EntityType entity, Operation operation) {

        // has specific permission on own entity type eg. device_read_own
        Permission p = new Permission(entity, operation, true);
        boolean hasPermission = u.getAuthorities().contains(p);
        if (hasPermission) {

            String ownerId = null;
            if (obj instanceof Owneable) {
                ownerId = ((Owneable) obj).getOwnerId();
            }

            if (obj instanceof String) {
                ownerId = (String) obj;
            }

            if (ownerId == null) {
                return false;
            }

            return ownerId.equals(u.getUuid());
        }
        return false;
    }

}
