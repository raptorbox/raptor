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
package org.createnet.raptor.auth.service.services;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.createnet.raptor.auth.service.acl.RaptorPermission;
import org.createnet.raptor.auth.service.acl.UserSid;
import org.createnet.raptor.models.auth.Device;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;

/**
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class AclDeviceService implements AclServiceInterface<Device> {

    private final Logger logger = LoggerFactory.getLogger(AclDeviceService.class);

    @Autowired
    private AclManagerService aclManagerService;

    protected Permission[] defaultPermissions = new Permission[]{
        RaptorPermission.READ,
        RaptorPermission.WRITE,};

    public void add(Device device, User user, Permission permission) {
        aclManagerService.addPermission(Device.class, device.getId(), new UserSid(user), permission);
    }

    @Override
    public void add(Device device, User user, List<Permission> permissions) {
        aclManagerService.addPermissions(Device.class, device.getId(), new UserSid(user), permissions);
    }

    @Override
    public void set(Device device, User user, List<Permission> permissions) {
        Long pid = null;
        if (device.hasParent()) {
            pid = device.getParent().getId();
        }
        aclManagerService.setPermissions(Device.class, device.getId(), new UserSid(user), permissions, pid);
    }

    @Override
    public List<Permission> list(Device device, User user) {
        ObjectIdentity oiDevice = new ObjectIdentityImpl(Device.class, device.getId());
        return aclManagerService.getPermissionList(user, oiDevice);
    }

    @Override
    public void remove(Device device, User user, Permission permission) {
        aclManagerService.removePermission(Device.class, device.getId(), new UserSid(user), permission);
    }

    @Override
    public boolean isGranted(Device device, User user, Permission permission) {
        return aclManagerService.isPermissionGranted(Device.class, device.getId(), new UserSid(user), permission);
    }

    @Retryable(maxAttempts = 3, value = AclManagerService.AclManagerException.class, backoff = @Backoff(delay = 500, multiplier = 3))
    @Override
    public void register(Device device) {

        User owner = device.getOwner();
        List<Permission> permissions = list(device, owner);
        Sid sid = new UserSid(owner);

        logger.debug("Found {} permissions for {}", permissions.size(), owner.getUuid());

        if (permissions.isEmpty()) {

            logger.debug("Set default permission");
            List<Permission> newPerms = Arrays.stream(defaultPermissions).collect(Collectors.toList());

            if (owner.getId().equals(device.getOwner().getId())) {
                newPerms.add(RaptorPermission.ADMINISTRATION);
            }

            try {
                aclManagerService.addPermissions(Device.class, device.getId(), sid, newPerms, device.getParentId());
            } catch (AclManagerService.AclManagerException ex) {
                logger.warn("Failed to store default permission for {} ({}): {}", device.getId(), sid, ex.getMessage());
                throw ex;
            }

            permissions.addAll(newPerms);
        } else {
            aclManagerService.setParent(device.getClass(), device.getId(), device.getParentId());
        }

        String perms = String.join(", ", RaptorPermission.toLabel(permissions));
        logger.debug("Permission set for device {} to {} - {}", device.getUuid(), device.getOwner().getUuid(), perms);

    }

    @Override
    public boolean check(Device device, User user, Permission permission) {

        if (user == null) {
            return false;
        }
        if (device == null && permission != RaptorPermission.LIST) {
            return false;
        }
        if (permission == null) {
            return false;
        }

        if (user.isSuperAdmin()) {
            return true;
        }

        if (!user.isEnabled()) {
            return false;
        }

        // check if user has ADMINISTRATION permission on device 
        if (isGranted(device, user, RaptorPermission.ADMINISTRATION)) {
            return true;
        }

        // check device specific permission first
        if (isGranted(device, user, permission)) {
            return true;
        }

        // check parent permission if available
        if (device != null && device.hasParent()) {
            return check(device.getParent(), user, permission);
        }

        return false;
    }

}
