/*
 * Copyright 2016 CREATE-NET
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

import org.createnet.raptor.auth.entity.SyncRequest;
import org.createnet.raptor.auth.service.acl.RaptorPermission;
import org.createnet.raptor.auth.service.entity.Device;
import org.createnet.raptor.auth.service.entity.User;
import org.createnet.raptor.auth.service.entity.repository.DeviceRepository;
import org.createnet.raptor.auth.service.entity.repository.UserRepository;
import org.createnet.raptor.auth.service.exception.DeviceNotFoundException;
import org.createnet.raptor.auth.service.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class DeviceService {

  protected static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private DeviceRepository deviceRepository;

  @Autowired
  private AclDeviceService aclObjectService;

  public Device save(Device device) {
    
    deviceRepository.save(device);
    aclObjectService.register(device);
    
    return device;
  }

  public Device getByUuid(String uuid) {
    return deviceRepository.findByUuid(uuid);
  }

  public Device get(Long id) {
    return deviceRepository.findOne(id);
  }

  public Device sync(Authentication auth, SyncRequest req) {

    Permission p = RaptorPermission.fromLabel(req.operation);
    
    Device device = null;
    if (req.objectId != null) {
      device = deviceRepository.findByUuid(req.objectId);
    }
    
    // delete device record
    if(p == RaptorPermission.DELETE) {
      
      if (device == null) {
        throw new DeviceNotFoundException();
      }
      
      deviceRepository.delete(device);
      return null;
    }
    
    // create or update device record
    if (device == null) {
      device = new Device();
      device.setUuid(req.objectId);
    }

    if (req.userId == null) {
      device.setOwner((User) auth.getPrincipal());
    } else {
      User owner = userRepository.findByUuid(req.userId);
      if (owner == null) {
        throw new UserNotFoundException();
      }
      device.setOwner(owner);
    }

    if (req.parentId != null) {
      Device parentDevice = deviceRepository.findByUuid(req.parentId);
      if (parentDevice == null) {
        throw new DeviceNotFoundException();
      }
      device.setParent(parentDevice);
    }
    
    Device dev = save(device);   
    
    return dev;
  }

}
