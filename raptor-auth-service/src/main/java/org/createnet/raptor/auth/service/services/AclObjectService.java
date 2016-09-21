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

import org.createnet.raptor.auth.service.acl.RaptorPermission;
import org.createnet.raptor.auth.service.acl.entity.AclServiceObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.Permission;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class AclObjectService {

  private final Logger log = LoggerFactory.getLogger(AclObjectService.class);
  
  @Autowired
  private AclManagerService aclManagerService;
  
  protected Permission[] defaultAcls = new Permission[] {
    RaptorPermission.WRITE,
  };
  
  public void register(AclServiceObject obj) {
    
  }

}
