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

import java.util.stream.Collectors;
import org.createnet.raptor.models.auth.Permission;
import org.createnet.raptor.auth.repository.PermissionRepository;
import org.createnet.raptor.auth.repository.PermissionRepository;
import org.createnet.raptor.models.auth.AclApp;
import org.createnet.raptor.models.auth.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
@CacheConfig(cacheNames = "permissions")
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;
    
    @CacheEvict(key = "#permission.name")
    public Permission save(Permission permission) {
        permissionRepository.save(permission);
        return getByName(permission.getName());
    }

    public Iterable<Permission> list() {
        return permissionRepository.findAll();
    }
    
    @CacheEvict(key = "#name")
    public void delete(String name) {
        permissionRepository.deleteByName(name);
    }
    
    public void delete(Permission p) {
        delete(p.getName());
    }

    @Cacheable(key = "#name")
    public Permission getByName(String name) {
        return permissionRepository.findOneByName(name);
    }
    
    public Permission getById(Long id) {
        return permissionRepository.findOneById(id);
    }

}
