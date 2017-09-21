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

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
//@CacheConfig(cacheNames = "acl_device")
public class AuthDeviceService {

    protected static final Logger logger = LoggerFactory.getLogger(AuthDeviceService.class);

    @Autowired
    private UserRepository userRepository;
    
    //@CacheEvict(key = "#device.id")
    public AclDevice save(AclDevice device) {

        AclDevice saved = deviceRepository.save(device);
        aclDeviceService.register(device);

        return get(saved.getId());
    }

    public AclDevice getByUuid(String uuid) {
        return deviceRepository.findByUuid(uuid);
    }

    //@Cacheable(key = "#id")
    public AclDevice get(Long id) {
        return deviceRepository.findOne(id);
    }
    
    //@CacheEvict(key = "#id")
    public void delete(Long id) {
        deviceRepository.delete(id);
    }
    
    public void delete(AclDevice device) {
        delete(device.getId());
    }
    
    public void sync(User user, SyncRequest req) {

        Permission p = RaptorPermission.fromLabel(req.operation);

        AclDevice device = null;
        if (req.objectId != null) {
            device = deviceRepository.findByUuid(req.objectId);
        }

        /**
         * @TODO check user permissions and roles
         */
        
        if(req.userId == null) {
            req.userId = user.getUuid();
        }
        
        if (!req.userId.equals(user.getUuid())) {
            if (!user.isSuperAdmin()) {
                if (!aclDeviceService.isGranted(device, user, RaptorPermission.ADMINISTRATION)) {
                    throw new AccessDeniedException("Cannot operate on that object");
                }
            }
        }

        // delete device record
        if (p == RaptorPermission.DELETE) {

            if (device == null) {
                throw new DeviceNotFoundException();
            }

            deviceRepository.delete(device);
            return;
        }

        // create or update device record
        if (device == null) {
            device = new AclDevice();
            device.setUuid(req.objectId);
        }

        if (req.userId == null) {
            device.setOwner(user);
        } else {
            User owner = userRepository.findByUuid(req.userId);
            if (owner == null) {
                throw new UserNotFoundException();
            }
            device.setOwner(owner);
        }

        if (req.parentId != null) {
            AclDevice parentDevice = deviceRepository.findByUuid(req.parentId);
            if (parentDevice == null) {
                throw new DeviceNotFoundException();
            }
            device.setParent(parentDevice);
        }

        AclDevice dev = save(device);

        return dev;
    }

}
