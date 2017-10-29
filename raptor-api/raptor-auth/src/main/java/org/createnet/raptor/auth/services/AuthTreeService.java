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

import org.createnet.raptor.models.auth.request.SyncRequest;
import org.createnet.raptor.models.acl.permission.RaptorPermission;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.auth.repository.UserRepository;
import org.createnet.raptor.auth.exception.DeviceNotFoundException;
import org.createnet.raptor.auth.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.model.Permission;
import org.springframework.stereotype.Service;
import org.createnet.raptor.auth.repository.AclTreeRepository;
import org.createnet.raptor.models.auth.AclTreeNode;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
@CacheConfig(cacheNames = "acltree")
public class AuthTreeService {

    protected static final Logger logger = LoggerFactory.getLogger(AuthTreeService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AclTreeRepository treeRepository;

    @Autowired
    private AclTreeService aclTreeService;

    @CacheEvict(key = "#tree.id")
    public AclTreeNode save(AclTreeNode node) {

        AclTreeNode saved = treeRepository.save(node);
        aclTreeService.register(node);

        return get(saved.getId());
    }

    public AclTreeNode getByUuid(String uuid) {
        return treeRepository.findByUuid(uuid);
    }

    @Cacheable(key = "#id")
    public AclTreeNode get(Long id) {
        return treeRepository.findOne(id);
    }

    @CacheEvict(key = "#id")
    public void delete(Long id) {
        treeRepository.delete(id);
    }

    public void delete(AclTreeNode tree) {
        delete(tree.getId());
    }

    public AclTreeNode sync(User user, SyncRequest req) {

        Permission p = RaptorPermission.fromLabel(req.operation);

        AclTreeNode tree = null;
        if (req.objectId != null) {
            tree = treeRepository.findByUuid(req.objectId);
        }

        /**
         * @TODO check user permissions and roles
         */
        if (req.userId == null) {
            req.userId = user.getUuid();
        }

        if (!req.userId.equals(user.getUuid())) {
            if (!user.isAdmin()) {
                if (!aclTreeService.isGranted(tree, RaptorPermission.ADMINISTRATION)) {
                    throw new AccessDeniedException("Cannot operate on that object");
                }
            }
        }

        // delete tree record
        if (p == RaptorPermission.DELETE) {
            if (tree == null) {
                throw new DeviceNotFoundException();
            }
            treeRepository.delete(tree);
            return null;
        }

        // create or update tree record
        if (tree == null) {
            tree = new AclTreeNode();
            tree.setUuid(req.objectId);
        }

        if (req.userId == null) {
            tree.setOwner(user);
        } else {
            User owner = userRepository.findOneByUuid(req.userId);
            if (owner == null) {
                throw new UserNotFoundException();
            }
            tree.setOwner(owner);
        }

        AclTreeNode dev = save(tree);
        return dev;
    }

}
