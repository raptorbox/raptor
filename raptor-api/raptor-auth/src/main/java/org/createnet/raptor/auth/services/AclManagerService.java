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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.createnet.raptor.auth.acl.AclManager;
import org.createnet.raptor.models.acl.permission.RaptorPermission;
import org.createnet.raptor.models.acl.AclSubject;
import org.createnet.raptor.models.acl.UserSid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
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
    public MutableAcl getACL(ObjectIdentity oi) {
        try {
            MutableAcl acl = isNewAcl(oi);
            return acl;
        } catch (Exception e) {
            throw new AclManagerException(e);
        }
    }

    @Retryable(maxAttempts = 3, value = AclManagerException.class, backoff = @Backoff(delay = 500, multiplier = 2))
    public void setParent(AclSubject subj) {

        ObjectIdentity oi = subj.getObjectIdentity();
        ObjectIdentity poi = subj.getParent().getObjectIdentity();

        try {

            MutableAcl childAcl = getACL(oi);
            if (poi != null) {
                MutableAcl parentAcl = getACL(poi);
                childAcl.setEntriesInheriting(true);
                childAcl.setParent(parentAcl);
            }

            aclService.updateAcl(childAcl);
        } catch (Exception e) {
            log.error("Failed to set parent pid:{} -> cid:{}", poi.toString(), oi.toString());
            throw new AclManagerException(e);
        }
    }

    @Override
    public <T> void addPermission(AclSubject subj, Permission permission) {
        addPermissions(subj, Arrays.asList(permission));
    }

    @Retryable(maxAttempts = 3, value = AclManagerException.class, backoff = @Backoff(delay = 500, multiplier = 2))
    public <T> void addPermissions(AclSubject subj, List<Permission> permissions) {

        UserSid sid = subj.getSid();
        ObjectIdentity oi = subj.getObjectIdentity();

        try {

            log.debug("Storing ACL {} {} {}", sid, String.join(",", RaptorPermission.toLabel(permissions)), oi.toString());

            MutableAcl acl = getACL(oi);
            permissions.stream().forEach((Permission p) -> {
                isPermissionGranted(p, sid, acl);
            });

            if (subj.getParent() != null) {
                log.debug("Setting parent ACL to {}", subj.getParent().toString());
                MutableAcl parentAcl = getACL(subj.getParent().getObjectIdentity());
                acl.setEntriesInheriting(true);
                acl.setParent(parentAcl);
            }

            aclService.updateAcl(acl);

        } catch (NotFoundException ex) {
            log.debug("Storing ACL FAILED for {} {} {}", sid, String.join(",", RaptorPermission.toLabel(permissions)), oi.toString());
            throw new AclManagerException(ex);
        }
    }

    @Override
    public <T> void removePermission(AclSubject subj, Permission permission) {

        UserSid sid = subj.getSid();
        ObjectIdentity oi = subj.getObjectIdentity();

        MutableAcl acl = (MutableAcl) aclService.readAclById(oi);
        AccessControlEntry[] entries = acl.getEntries().toArray(new AccessControlEntry[acl.getEntries().size()]);

        for (int i = 0; i < acl.getEntries().size(); i++) {
            if (entries[i].getSid().equals(sid) && entries[i].getPermission().equals(permission)) {
                acl.deleteAce(i);
            }
        }

        aclService.updateAcl(acl);
    }

    @Override
    public <T> boolean isPermissionGranted(AclSubject subj, Permission permission) {

        UserSid sid = subj.getSid();
        ObjectIdentity oi = subj.getObjectIdentity();

        MutableAcl acl;
        try {
            acl = (MutableAcl) aclService.readAclById(oi);
        } catch (NotFoundException ex) {
            log.warn("ACL not found for {}: {}", oi.toString(), ex.getMessage());
            return false;
        }

        boolean isGranted = false;

        try {
            log.debug("Check if {} can {} on {}", sid, RaptorPermission.toLabel(permission), oi.toString());
            isGranted = acl.isGranted(Arrays.asList(permission), Arrays.asList(sid), false);
            log.debug("User={} is {}ALLOWED to `{}` on {}", sid, (isGranted ? "" : "NOT "), RaptorPermission.toLabel(permission), oi.toString());
        } catch (NotFoundException e) {
            log.info("No permission `{}` found for {} - {}", RaptorPermission.toLabel(permission), oi.toString(), e.getMessage());
        } catch (UnloadedSidException e) {
            log.error("Unloaded Sid for {} on {} - {}", RaptorPermission.toLabel(permission), oi.toString(), e.getMessage(), e);
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

    public void deleteAllGrantedAcl() {
//        jdbcTemplate.update("delete from acl_entry");
//        jdbcTemplate.update("delete from acl_object_identity");
//        jdbcTemplate.update("delete from acl_sid");
//        jdbcTemplate.update("delete from acl_class");
    }

    public List<Permission> getPermissionList(AclSubject subj) {

        UserSid sid = subj.getSid();
        ObjectIdentity oi = subj.getObjectIdentity();

        List<Permission> permissionsList = new ArrayList();

        // Lookup only ACLs for SIDs we're interested in
        Acl acl = null;
        try {
            acl = aclService.readAclById(oi, Arrays.asList(sid));
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

    public void setPermissions(AclSubject subj, List<Permission> permissions) {

        UserSid sid = subj.getSid();
        ObjectIdentity oi = subj.getObjectIdentity();

        List<Permission> currentPermissions = getPermissionList(subj);
        currentPermissions.forEach((Permission permission) -> {
            removePermission(subj, permission);
        });

        addPermissions(subj, permissions);
    }
}
