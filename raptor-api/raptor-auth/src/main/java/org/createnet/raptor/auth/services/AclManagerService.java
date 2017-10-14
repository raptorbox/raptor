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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.createnet.raptor.auth.acl.AclManager;
import org.createnet.raptor.auth.acl.RaptorPermission;
import org.createnet.raptor.auth.acl.UserSid;
import org.createnet.raptor.models.auth.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Transactional
@Service
public class AclManagerService implements AclManager {

    private final Logger log = LoggerFactory.getLogger(AclManagerService.class);

    public class AclManagerException extends RuntimeException {

        public AclManagerException(Throwable cause) {
            super(cause);
        }

    }

    @Autowired
    private MutableAclService aclService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final SidRetrievalStrategy sidRetrievalStrategy = new SidRetrievalStrategyImpl();

    @Retryable(maxAttempts = 3, value = AclManagerException.class, backoff = @Backoff(delay = 500, multiplier = 2))
    public MutableAcl getACL(Class clazz, Serializable identifier) {
        try {
            ObjectIdentity identity = new ObjectIdentityImpl(clazz, identifier);
            MutableAcl acl = isNewAcl(identity);
            return acl;
        } catch (Exception e) {
            throw new AclManagerException(e);
        }
    }

    @Retryable(maxAttempts = 3, value = AclManagerException.class, backoff = @Backoff(delay = 500, multiplier = 2))
    public void setParent(Class<?> clazz, Long childId, Long parentId) {
        try {
            
            MutableAcl childAcl = getACL(clazz, childId);
            if(parentId != null) {
                MutableAcl parentAcl = getACL(clazz, parentId);
                childAcl.setEntriesInheriting(true);
                childAcl.setParent(parentAcl);
            }

            aclService.updateAcl(childAcl);
        }
        catch(Exception e) {
            log.error("Failed to set parent pid:{} -> cid:{}", parentId, childId);
            throw new AclManagerException(e);
        }
    }
    
    @Override
    public <T> void addPermission(Class<T> clazz, Serializable identifier, Sid sid, Permission permission) {
        addPermissions(clazz, identifier, sid, Arrays.asList(permission), null);
    }

    public <T> void addPermission(Class<T> clazz, Serializable identifier, Sid sid, Permission permission, Long parentId) {
        addPermissions(clazz, identifier, sid, Arrays.asList(permission), parentId);
    }

    public <T> void addPermissions(Class<T> clazz, Serializable identifier, Sid sid, List<Permission> permissions) {
        addPermissions(clazz, identifier, sid, permissions, null);
    }

    @Retryable(maxAttempts = 3, value = AclManagerException.class, backoff = @Backoff(delay = 500, multiplier = 2))
    public <T> void addPermissions(Class<T> clazz, Serializable identifier, Sid sid, List<Permission> permissions, Long parentId) {
        try {

            log.debug("Storing ACL {} {} {}:{}", sid, String.join(",", RaptorPermission.toLabel(permissions)), clazz, identifier);

            MutableAcl acl = getACL(clazz, identifier);
            permissions.stream().forEach((Permission p) -> {
                isPermissionGranted(p, sid, acl);
            });

            if (parentId != null) {
                log.debug("Setting parent ACL to {}", parentId);
                MutableAcl parentAcl = getACL(clazz, parentId);
                acl.setEntriesInheriting(true);
                acl.setParent(parentAcl);
            }

            aclService.updateAcl(acl);

        } catch (NotFoundException ex) {
            log.debug("Storing ACL FAILED for {} {} {}:{}", sid, String.join(",", RaptorPermission.toLabel(permissions)), clazz, identifier);
            throw new AclManagerException(ex);
        }
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
        
        MutableAcl acl;
        try {
            acl = (MutableAcl) aclService.readAclById(identity);
        }
        catch(NotFoundException ex) {
            log.warn("ACL not found for type:{} id:{}. Ex:{}", clazz, identifier, ex.getMessage());
            return false;
        }
        
        boolean isGranted = false;

        try {
            log.debug("Check if {} can {} on {}:{}", sid, RaptorPermission.toLabel(permission), clazz, identifier);
            isGranted = acl.isGranted(Arrays.asList(permission), Arrays.asList(sid), false);
            log.debug("User={} is {}ALLOWED to `{}` on {}:{}", sid, (isGranted ? "" : "NOT "), RaptorPermission.toLabel(permission), clazz, identifier);
        } catch (NotFoundException e) {
            log.info("No permission `{}` found for {}:{} - {}", RaptorPermission.toLabel(permission), clazz, identifier, e.getMessage());
        } catch (UnloadedSidException e) {
            log.error("Unloaded Sid for {} on {}:{} - {}", RaptorPermission.toLabel(permission), clazz, identifier, e.getMessage(), e);
        }

        return isGranted;
    }

    private MutableAcl isNewAcl(ObjectIdentity identity) {
        MutableAcl acl = null;
        try {
            acl = (MutableAcl) aclService.readAclById(identity);
        } catch (NotFoundException e) {
            acl = aclService.createAcl(identity);
        }
        return acl;
    }

    @Retryable(maxAttempts = 3, value = AclManagerException.class, backoff = @Backoff(delay = 200, multiplier = 3))
    private void isPermissionGranted(Permission permission, Sid sid, MutableAcl acl) {
        try {
            try {
                acl.isGranted(Arrays.asList(permission), Arrays.asList(sid), false);
            } catch (NotFoundException e) {
                acl.insertAce(acl.getEntries().size(), permission, sid, true);
            }
        } catch (Exception e) {
            log.warn("Failed to add ACE: {}", e.getMessage());
            throw new AclManagerException(e);
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
        aces.stream().forEach((ace) -> {
            String aceUuid = ((PrincipalSid) ace.getSid()).getPrincipal();
            String sidUuid = sid.getUser().getUuid();
            if (!(!sidUuid.equals(aceUuid))) {
                if (!(!ace.isGranting())) {
                    permissionsList.add(ace.getPermission());
                }
            }
        });

        return permissionsList;
    }

    public void setPermissions(Class<?> clazz, Long identifier, UserSid userSid, List<Permission> permissions, Long parentId) {

        ObjectIdentity identity = new ObjectIdentityImpl(clazz, identifier);

        List<Permission> currentPermissions = getPermissionList(userSid.getUser(), identity);
        currentPermissions.forEach((Permission permission) -> {
            removePermission(clazz, identifier, userSid, permission);
        });

        addPermissions(clazz, identifier, userSid, permissions, parentId);
    }
}
