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
package org.createnet.raptor.auth.services;

import java.util.List;
import org.springframework.security.acls.model.Permission;
import org.createnet.raptor.models.auth.User;

/**
 * @author Luca Capra <lcapra@fbk.eu>
 * @param <S> subject type
 */
public interface AclServiceInterface<S> {

    public void add(S subject, User user, List<Permission> permissions);

    public void set(S subject, User user, List<Permission> permissions);
    public List<Permission> list(S subject, User user);
    public void remove(S subject, User user, Permission permission);

    public boolean isGranted(S subject, User user, Permission permission);
    public void register(S subject);
    public boolean check(S subject, User user, Permission permission);

}