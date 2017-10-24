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
package org.createnet.raptor.models.acl;

import org.createnet.raptor.models.auth.AclApp;
import org.createnet.raptor.models.auth.AclDevice;
import org.createnet.raptor.models.tree.TreeNode;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class AclClassTypeMapper {

    public static enum Type {
        device, 
        tree, 
        app
    }

    public static Class device = AclDevice.class;
    public static Class tree = TreeNode.class;
    public static Class app = AclApp.class;    
    

    public static Class get(String name) {
        return get(Type.valueOf(name));
    }

    public static Class get(Type name) {
        switch (name) {
            case device:
                return device;
            case tree:
                return tree;
            case app:
                return app;
            default:
                throw new RuntimeException(String.format("Permission subject `%s` does not exists", name));
        }
    }

}
