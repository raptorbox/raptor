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
import org.createnet.raptor.auth.service.entity.Device;
import org.createnet.raptor.auth.service.entity.User;
import org.createnet.raptor.auth.service.entity.repository.DeviceRepository;
import org.createnet.raptor.auth.service.entity.repository.UserRepository;
import org.createnet.raptor.auth.service.exception.DeviceNotFoundException;
import org.createnet.raptor.auth.service.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
  private AclObjectService aclObjectService;

  protected Device save(Device device) {
    return deviceRepository.save(device);
  }

  public Device sync(Authentication auth, SyncRequest req) {

    Device device = null;
    if (req.objectId != null) {
      device = deviceRepository.findByUuid(req.objectId);
    }

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
    
    aclObjectService.sync(auth, dev);
    
    return dev;
  }

}
