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
package org.createnet.raptor.auth.acl;

import java.util.List;
import org.createnet.raptor.models.acl.AclSubject;
import org.springframework.security.acls.model.Permission;

/**
 * @author Luca Capra <lcapra@fbk.eu>
 * @param <T>
 */
public interface AclServiceInterface<T extends AclSubject> {

    public List<Permission> getDefaultPermissions();

    public void add(T subj, List<Permission> permissions);
    public void set(T subj, List<Permission> permissions);
    public List<Permission> list(T subj);
    public void remove(T subj, Permission permission);

    public boolean isGranted(T subj, Permission permission);
    public void register(T subj);
    public boolean check(T subj, Permission permission);
    public T load(Long id);

}