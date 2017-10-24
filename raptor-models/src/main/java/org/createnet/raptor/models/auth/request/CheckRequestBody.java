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
package org.createnet.raptor.models.auth.request;

import java.util.List;
import java.util.UUID;
import javax.validation.ValidationException;
import org.createnet.raptor.models.acl.permission.RaptorPermission;
import org.springframework.security.acls.model.Permission;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class CheckRequestBody {

  public String permission;
  public String objectId;
  public List<String> tree;
  
  protected boolean validateUUID(String uuid, String fieldName) throws ValidationException {

    if (objectId == null) {
      throw new ValidationException("Permission missing or not valid");
    }

    try {
      UUID.fromString(uuid);
    } catch (IllegalArgumentException e) {
      throw new ValidationException("Cannot parse UUID from " + fieldName);
    }

    return true;
  }

  public boolean validate() {

    Permission p = RaptorPermission.fromLabel(permission);
    if (p == null) {
      throw new ValidationException("Permission missing or not valid");
    }

    validateUUID(objectId, "objectId");

    tree.stream().forEach((s) -> {
      validateUUID(s, "tree");
    });

    return true;
  }

}
