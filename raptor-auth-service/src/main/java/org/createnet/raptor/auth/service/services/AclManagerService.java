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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.createnet.raptor.auth.service.acl.AclManager;
import org.createnet.raptor.auth.service.acl.UserSid;
import org.createnet.raptor.auth.service.entity.Device;
import org.createnet.raptor.auth.service.entity.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.SidRetrievalStrategyImpl;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.acls.model.SidRetrievalStrategy;
import org.springframework.security.acls.model.UnloadedSidException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Transactional
@Service
public class AclManagerService implements AclManager {

  private final Logger log = LoggerFactory.getLogger(AclManagerService.class);

  @Autowired
  private MutableAclService aclService;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private final SidRetrievalStrategy sidRetrievalStrategy = new SidRetrievalStrategyImpl();

  @Override
  public <T> void addPermission(Class<T> clazz, Serializable identifier, Sid sid, Permission permission) {
    ObjectIdentity identity = new ObjectIdentityImpl(clazz, identifier);
    MutableAcl acl = isNewAcl(identity);
    isPermissionGranted(permission, sid, acl);
    aclService.updateAcl(acl);
  }

  public <T> void addPermissions(Class<T> clazz, Serializable identifier, Sid sid, List<Permission> permissions) {
    permissions.stream().forEach((Permission p) -> {
      addPermission(clazz, identifier, sid, p);
    });
  }

  @Override
  public <T> void removePermission(Class<T> clazz, Serializable identifier, Sid sid, Permission permission) {
    ObjectIdentity identity = new ObjectIdentityImpl(clazz.getCanonicalName(), identifier);
    MutableAcl acl = (MutableAcl) aclService.readAclById(identity);

    AccessControlEntry[] entries = acl.getEntries().toArray(new AccessControlEntry[acl.getEntries().size()]);

    for (int i = 0; i < acl.getEntries().size(); i++) {
      if (entries[i].getSid().equals(sid) && entries[i].getPermission().equals(permission)) {
        acl.deleteAce(i);
      }
    }

    aclService.updateAcl(acl);
  }

  @Override
  public <T> boolean isPermissionGranted(Class<T> clazz, Serializable identifier, Sid sid, Permission permission) {
    ObjectIdentity identity = new ObjectIdentityImpl(clazz.getCanonicalName(), identifier);
    MutableAcl acl = (MutableAcl) aclService.readAclById(identity);
    boolean isGranted = false;

    try {
      isGranted = acl.isGranted(Arrays.asList(permission), Arrays.asList(sid), false);
    } catch (NotFoundException e) {
      log.info("Unable to find an ACE for the given object", e);
    } catch (UnloadedSidException e) {
      log.error("Unloaded Sid", e);
    }

    return isGranted;
  }

  private MutableAcl isNewAcl(ObjectIdentity identity) {
    MutableAcl acl;
    try {
      acl = (MutableAcl) aclService.readAclById(identity);
    } catch (NotFoundException e) {
      acl = aclService.createAcl(identity);
    }
    return acl;
  }

  private void isPermissionGranted(Permission permission, Sid sid, MutableAcl acl) {
    try {
      acl.isGranted(Arrays.asList(permission), Arrays.asList(sid), false);
    } catch (NotFoundException e) {
      acl.insertAce(acl.getEntries().size(), permission, sid, true);
    }
  }

  @Override
  public void deleteAllGrantedAcl() {
    jdbcTemplate.update("delete from acl_entry");
    jdbcTemplate.update("delete from acl_object_identity");
    jdbcTemplate.update("delete from acl_sid");
    jdbcTemplate.update("delete from acl_class");
  }

  public List<Permission> getPermissionList(User user, ObjectIdentity oid) {
    return getPermissionList(new UserSid(user), oid);
  }

  public List<Permission> getPermissionList(UserSid sid, ObjectIdentity oid) {

    List<Permission> permissionsList = new ArrayList();

    // Lookup only ACLs for SIDs we're interested in
    Acl acl = null;
    try {
      acl = aclService.readAclById(oid, Arrays.asList(sid));
    } catch (Exception e) {
      return permissionsList;
    }

    List<AccessControlEntry> aces = acl.getEntries();
    for (AccessControlEntry ace : aces) {
      
      String siduuid = ace.getSid().toString();
      if(!sid.getUser().getUuid().equals(siduuid))
        continue;
      
      if(!ace.isGranting())
        continue;
      
      permissionsList.add(ace.getPermission());
    }

    return permissionsList;
  }

  public void setPermissions(Class<?> clazz, Long identifier, UserSid userSid, List<Permission> permissions) {

    ObjectIdentity identity = new ObjectIdentityImpl(clazz, identifier);

    List<Permission> currentPermissions = getPermissionList(userSid.getUser(), identity);
    currentPermissions.forEach((Permission permission) -> {
      removePermission(clazz, identifier, userSid, permission);
    });

    addPermissions(clazz, identifier, userSid, permissions);
  }
}
