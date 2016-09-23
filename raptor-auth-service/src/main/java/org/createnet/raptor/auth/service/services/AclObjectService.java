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

import java.util.List;
import org.createnet.raptor.auth.service.acl.RaptorPermission;
import org.createnet.raptor.auth.service.entity.Device;
import org.createnet.raptor.auth.service.entity.repository.DeviceRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class AclObjectService {

  private final Logger logger = LoggerFactory.getLogger(AclObjectService.class);

  @Autowired
  private AclManagerService aclManagerService;

  @Autowired
  private DeviceRepository deviceRepository;

  protected Permission[] defaultAcls = new Permission[]{
    RaptorPermission.READ,
    RaptorPermission.WRITE,
  };

  public void sync(Authentication auth, Device device) {

    ObjectIdentity oiDevice = new ObjectIdentityImpl(device.getClass(), device.getId());
    List<Permission> permissions = aclManagerService.getPermissionList(auth, oiDevice);

    
    logger.debug("Found {} permissions", permissions.size());

  }

}
