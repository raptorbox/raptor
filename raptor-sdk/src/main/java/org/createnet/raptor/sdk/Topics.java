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
package org.createnet.raptor.sdk;

/**
 * List of topics for Raptor API
 */
final public class Topics {
    
    public enum Types {
        action, stream, device, user, tree, token, app
    }
    
    public static final String ACTION = Types.action.name() + "/%s/%s";
    public static final String STREAM = Types.stream.name() + "/%s/%s";
    public static final String DEVICE = Types.device.name() + "/%s";
    public static final String USER = Types.user.name() + "/%s";
    public static final String TOKEN = Types.token.name() + "/%s";
    public static final String TREE = Types.tree.name() + "/%s";
    public static final String APP = Types.app.name() + "/%s";
    
}
