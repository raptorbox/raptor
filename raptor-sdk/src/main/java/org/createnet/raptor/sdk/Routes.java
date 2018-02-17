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
 * List of base path for Raptor API
 */
final public class Routes {

    public static final String LIST = "/";
    public static final String SEARCH = "/search";
    public static final String CREATE = LIST;
    public static final String UPDATE = "/%s";
    public static final String LOAD = UPDATE;
    public static final String DELETE = UPDATE;
    
    public static final String STREAM_GET = "/stream/%s/%s";
    public static final String STREAM_PUSH = STREAM_GET;
    
    public static final String STREAM_LAST_UPDATE = STREAM_PUSH + "/lastUpdate";
    public static final String STREAM_PULL = STREAM_PUSH;
    public static final String STREAM_SEARCH = STREAM_PUSH;
    
    public static final String ACTION_INVOKE = "/action/%s/%s";
    public static final String ACTION_STATUS = ACTION_INVOKE;
    
    
    public static final String PERMISSION_SYNC = "/auth/sync";
    public static final String PERMISSION_CHECK = "/auth/check";
    public static final String LOGIN = "/auth/login";
    public static final String LOGOUT = LOGIN;
    public static final String REFRESH_TOKEN = "/auth/refresh";
    
    public static final String USER_CREATE = "/auth/user";
    public static final String USER_LIST = USER_CREATE;
    public static final String USER_GET = USER_CREATE + "/%s";
    public static final String USER_IMPERSONATE = USER_GET + "/impersonate";
    public static final String USER_UPDATE = USER_GET;
    public static final String USER_DELETE = USER_GET;
    public static final String USER_DELETE_OWNERID = USER_GET + "?ownerId=%s";
    public static final String USER_GET_ME = "/auth/me";
    public static final String USER_UPDATE_ME = USER_GET_ME;
    
    public static final String TOKEN_CREATE = "/auth/token";
    public static final String TOKEN_UPDATE = TOKEN_CREATE + "/%s";
    public static final String TOKEN_GET = TOKEN_UPDATE;
    public static final String TOKEN_DELETE = TOKEN_UPDATE;
    public static final String TOKEN_LIST = TOKEN_CREATE;
    public static final String TOKEN_CURRENT = TOKEN_CREATE + "/current";
    
    public static final String PROFILE_GET_ALL = "/profile/%s";
    public static final String PROFILE_GET = PROFILE_GET_ALL + "/%s";
    public static final String PROFILE_SET = PROFILE_GET;
    public static final String PREFERENCES_DELETE = PROFILE_GET;
    
    public static final String PERMISSION_GET = "/auth/permission/%s/%s";
    public static final String PERMISSION_BY_USER = PERMISSION_GET + "/%s";
    public static final String PERMISSION_SET = PERMISSION_GET;
    
    public static final String INVENTORY_LIST = "/inventory/";
    public static final String INVENTORY_SEARCH = INVENTORY_LIST + "search";
    public static final String INVENTORY_CREATE = INVENTORY_LIST;
    public static final String INVENTORY_UPDATE = INVENTORY_LIST + "%s";
    public static final String INVENTORY_LOAD = INVENTORY_UPDATE;
    public static final String INVENTORY_DELETE = INVENTORY_UPDATE;
    
    public static final String TREE_LIST = "/tree/";    
    public static final String TREE_SEARCH = TREE_LIST + "search";
    public static final String TREE_CREATE = TREE_LIST;
    public static final String TREE_GET = TREE_LIST + "%s";
    public static final String TREE_CHILDREN = TREE_GET + "/children";
    public static final String TREE_ADD = TREE_CHILDREN;
    public static final String TREE_REMOVE = TREE_GET;
    public static final String TREE_REMOVE_TREE = TREE_GET + "tree";
    
    public static final String APP_CREATE = "/app";
    public static final String APP_LIST = APP_CREATE;
    public static final String APP_SEARCH = APP_CREATE + "/search";
    public static final String APP_READ = APP_CREATE + "/%s";
    public static final String APP_UPDATE = APP_READ;
    public static final String APP_DELETE = APP_READ;
    
    public static final String ROLE_CREATE = "/auth/role";
    public static final String ROLE_LIST = ROLE_CREATE;
    public static final String ROLE_READ = ROLE_CREATE + "/%s";
    public static final String ROLE_UPDATE = ROLE_READ;
    public static final String ROLE_DELETE = ROLE_READ;
    
}
