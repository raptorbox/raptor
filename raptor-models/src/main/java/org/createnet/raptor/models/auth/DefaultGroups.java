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
package org.createnet.raptor.models.auth;

import java.util.Arrays;
import org.createnet.raptor.models.acl.EntityType;
import org.createnet.raptor.models.acl.Operation;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class DefaultGroups {
    
    public static Group admin = new Group(StaticGroup.admin, Arrays.asList(
            new Permission(Operation.admin)
    ));
    
    public static Group user = new Group(StaticGroup.user, Arrays.asList(
            new Permission(EntityType.device, Operation.admin, true),
            new Permission(EntityType.tree, Operation.admin, true),
            new Permission(EntityType.app, Operation.admin, true)
    ));
    
}
