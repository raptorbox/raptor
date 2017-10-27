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

import org.createnet.raptor.models.acl.permission.RaptorPermission;
import java.util.Arrays;
import org.createnet.raptor.auth.services.*;
import java.util.List;
import java.util.stream.Collectors;
import org.createnet.raptor.models.acl.AclSubject;
import org.createnet.raptor.models.acl.AclSubjectImpl;
import org.createnet.raptor.models.auth.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.stereotype.Service;

/**
 * @author Luca Capra <lcapra@fbk.eu>
 * @param <T>
 */
@Service
public abstract class AbstractAclService<T extends AclSubject> implements AclServiceInterface<T> {

    private final Logger logger = LoggerFactory.getLogger(AbstractAclService.class);

    @Autowired
    protected AclManagerService aclManagerService;

    @Override
    abstract public List<Permission> getDefaultPermissions();

    @Override
    abstract public T load(Long id);

    protected List<Permission> getPermissions(T subj) {
        List<Permission> permissions = list(subj);
        if (permissions.isEmpty()) {
            // skip if no default permissions
            List<Permission> defaultPermissions = getDefaultPermissions();
            permissions.addAll(defaultPermissions);
        }
        return permissions;
    }

    protected void savePermissions(T subj, List<Permission> permissions) {

        User user = subj.getSid().getUser();
        ObjectIdentity oi = subj.getObjectIdentity();

        try {
            set(subj, permissions);
        } catch (AclManagerService.AclManagerException ex) {
            logger.warn("Failed to store permissions for {} (owner:{}): {}", oi.toString(), user.getUuid(), ex.getMessage());
            throw ex;
        }

        if (subj.getParent() != null) {
            logger.debug("Set node id:{} parent {} ", subj.getObjectIdentity().toString(), subj.getParent().toString());
            aclManagerService.setParent(subj);
        }

        String perms = String.join(", ", RaptorPermission.toLabel(permissions));
        logger.debug("Permission set for object {} to {} - {}", oi.getIdentifier(), user.getUuid(), perms);
    }

    @Override
    @Retryable(maxAttempts = 3, value = AclManagerService.AclManagerException.class, backoff = @Backoff(delay = 500, multiplier = 3))
    public void register(T subj) {
        savePermissions(subj, getPermissions(subj));
    }

    public void add(T subj, Permission permission) {
        add(subj, Arrays.asList(permission));
    }

    @Override
    public void add(T subj, List<Permission> permissions) {
        aclManagerService.addPermissions(subj, permissions);
    }

    @Override
    public void set(T subj, List<Permission> permissions) {

        // distinct
        permissions = permissions.stream().distinct().collect(Collectors.toList());
        logger.debug("Saving {} permissions for {}", permissions.size(), subj.getSid().getUser().getUuid());

        aclManagerService.setPermissions(subj, permissions);
    }

    @Override
    public List<Permission> list(T subj) {
        return aclManagerService.getPermissionList(subj);
    }

    @Override
    public void remove(T subj, Permission permission) {
        aclManagerService.removePermission(subj, permission);
    }

    @Override
    public boolean isGranted(T subj, Permission permission) {
        return aclManagerService.isPermissionGranted(subj, permission);
    }

    @Override
    public boolean check(T subj, Permission permission) {

        if (subj == null) {
            logger.debug("ACL: Subject is null");
            return false;
        }

        User user = subj.getSid().getUser();

        if (user == null) {
            logger.debug("ACL: User is null");
            return false;
        }

        if (permission == null) {
            logger.debug("ACL: Permission is null");
            return false;
        }

        if (user.isAdmin()) {
            logger.debug("ACL: User is admin");
            return true;
        }

        if (!user.isEnabled()) {
            logger.debug("ACL: User is not enabled");
            return false;
        }

        // check if user has ADMINISTRATION permission on subject
        if (isGranted(subj, RaptorPermission.ADMINISTRATION)) {
            logger.debug("ACL: User has ADMIN permission");
            return true;
        }

        // check subject specific permission first
        if (isGranted(subj, permission)) {
            logger.debug("ACL: User has `{}` permission", RaptorPermission.toLabel(permission));
            return true;
        }

        // check parent permission if available
        if (subj.getParent() != null) {
            logger.debug("ACL: Check inherited permissions");
            return check((T) subj.getParent(), permission);
        }

        logger.debug("ACL: Not allowed");
        return false;
    }

    public List<Permission> list(T subj, User user) {
        return list(createSubject(subj, user));
    }

    public boolean check(T subj, User user, Permission permission) {
        return check(createSubject(subj, user), permission);
    }

    public void set(T subj, User user, List<Permission> permissions) {
        set(createSubject(subj, user), permissions);
    }

    protected T createSubject(T subj, User user) {
        return (T) (new AclSubjectImpl(subj, user));
    }

}
