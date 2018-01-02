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
package org.createnet.raptor.models.acl.permission;

import java.util.ArrayList;
import java.util.List;
import org.createnet.raptor.models.acl.AclClassType;
import org.createnet.raptor.models.acl.EntityType;
import org.createnet.raptor.models.acl.ObjectPermission;
import org.createnet.raptor.models.acl.Operation;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.springframework.security.acls.model.Permission;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class PermissionUtil {

    /**
     * Generate a list of strings from a list of Operation
     *
     * @param args
     * @return
     */
    static public List<String> asList(Operation... args) {
        List<String> result = new ArrayList();
        for (Operation arg : args) {
            result.add(arg.name());
        }
        return result;
    }

    /**
     * Generate a list of Operation from a list of string
     *
     * @param args
     * @return
     */
    static public List<Operation> asList(String... args) {
        List<Operation> result = new ArrayList();
        for (String arg : args) {
            result.add(Operation.valueOf(arg));
        }
        return result;
    }

    static public List<String> generatePermissionList() {
        List<String> list = new ArrayList();
        Operation[] ops = new Operation[] {
            Operation.admin, Operation.create, Operation.update, Operation.delete, Operation.read
        };
        
        for (EntityType entity : EntityType.values()) {
            for (Operation operation : ops) {
                
                list.add(String.format("%s_%s", entity.name(), operation.name()));
                
                if (operation != Operation.create && operation != Operation.admin)
                    list.add(String.format("%s_%s_own", entity.name(), operation.name()));

            }
        }
        
        list.add(String.format("%s_%s", EntityType.device, Operation.push));
        list.add(String.format("%s_%s", EntityType.device, Operation.pull));
        
        list.add(String.format("%s_%s", EntityType.action, Operation.execute));
        
        return list;
    }

    static public ObjectPermission parseObjectPermission(String label) {

        label = label.toLowerCase().replace("role_", "");
        String[] parts = label.split("_");
        if (parts.length < 2 || parts.length > 3) {
            throw new RaptorComponent.ValidationException("Failed to parse permission " + label);
        }

        Class subjType;
        try {
            subjType = AclClassType.getClass(parts[0]);
        } catch (Exception ex) {
            throw new RaptorComponent.ValidationException("Failed to parse permission subject: " + parts[0], ex);
        }

        Permission perm = RaptorPermission.fromLabel(parts[1]);
        if (perm == null) {
            throw new RaptorComponent.ValidationException("Failed to parse operation: " + parts[1]);
        }
            
        boolean own = parts.length == 3;
        
        return new ObjectPermission(subjType, perm, own);
    }

}
