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

import org.createnet.raptor.auth.acl.AbstractAclService;
import org.createnet.raptor.auth.acl.RaptorPermission;
import org.createnet.raptor.models.auth.AclDevice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.Permission;
import org.springframework.stereotype.Service;

/**
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class AclDeviceService extends AbstractAclService<AclDevice> {

    private final Logger logger = LoggerFactory.getLogger(AclDeviceService.class);

    @Autowired
    AuthDeviceService deviceService;
    
    @Override
    public Permission[] getDefaultPermissions() {
        return new Permission[]{
            RaptorPermission.READ,
            RaptorPermission.WRITE
        };
    }

    @Override
    public AclDevice load(Long id) {
        return deviceService.get(id);
    }

}
