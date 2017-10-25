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

import org.createnet.raptor.models.auth.Group;
import org.createnet.raptor.auth.repository.GroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class GroupService {

    private static final Logger logger = LoggerFactory.getLogger(GroupService.class);

    @Autowired
    private GroupRepository roleRepository;

    public Group update(Long roleId, Group rawGroup) {

        Group role = roleRepository.findOne(roleId);

        if (role == null) {
            return null;
        }

        if (rawGroup.getName() != null && !rawGroup.getName().isEmpty()) {
            role.setName(rawGroup.getName());
        }

        return roleRepository.save(role);
    }

    public Iterable<Group> list() {
        return roleRepository.findAll();
    }

    public Group create(Group rawGroup) {

        Group existsGroup = roleRepository.findByName(rawGroup.getName());
        if (existsGroup != null) {
            return null;
        }

        Group role = new Group();
        role.setName(rawGroup.getName());

        return roleRepository.save(role);
    }

    public boolean delete(Long roleId) {

        if (!roleRepository.exists(roleId)) {
            return false;
        }

        roleRepository.delete(roleId);
        return true;
    }

    public Group getByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return roleRepository.findByName(name);
    }

}
