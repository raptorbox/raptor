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
import org.createnet.raptor.models.auth.Group;
import org.createnet.raptor.auth.repository.GroupRepository;
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
@CacheConfig(cacheNames = "groups")
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private AuthAppService appService;
    
    @CacheEvict(key = "#group.id")
    public Group save(Group group) {

        group.setPermissions(group.getPermissions().stream().map((p) -> {
            Permission perm = permissionService.getByName(p.getName());
            if (perm == null) {
                perm = permissionService.save(p);
            }
            return perm;
        }).collect(Collectors.toList()));
        
        String appUuid = group.getAppId();
        if(appUuid == null) {
            group.setApp(null);
        } else {
            AclApp app = appService.getByUuid(appUuid);
            group.setApp(app);
        }
        
        groupRepository.save(group);
        
        return getById(group.getId());
    }

    public Iterable<Group> list() {
        return groupRepository.findAll();
    }
    
    @CacheEvict(key = "#id")
    public void delete(Long id) {
        groupRepository.delete(id);
    }
    
    public void delete(Group g) {
        delete(g.getId());
    }

    public Group getByName(String name) {
        return groupRepository.findOneByName(name);
    }

    public Group getByNameAndApp(String name, AclApp app) {
        return groupRepository.findOneByNameAndApp(name, app);
    }
    
    @Cacheable(key = "#id")
    public Group getById(Long id) {
        return groupRepository.findOneById(id);
    }

}
