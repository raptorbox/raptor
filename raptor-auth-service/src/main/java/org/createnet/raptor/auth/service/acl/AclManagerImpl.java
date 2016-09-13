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
package org.createnet.raptor.auth.service.acl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import javax.sql.DataSource;
import org.createnet.raptor.auth.service.entity.Role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.SidRetrievalStrategyImpl;
import org.springframework.security.acls.domain.SpringCacheBasedAclCache;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.acls.model.SidRetrievalStrategy;
import org.springframework.security.acls.model.UnloadedSidException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Transactional
@Service
public class AclManagerImpl implements AclManager {

  private final Logger log = LoggerFactory.getLogger(AclManagerImpl.class);
  
  @Bean
  public MutableAclService aclService() {
    return new JdbcMutableAclService(dataSource, lookupStrategy(), aclCache());
  }
  
  @Bean
  public AclCache aclCache() {
    return new SpringCacheBasedAclCache(cacheManager.getCache("acl"), new PermissionGrantingStrategy() {
      @Override
      public boolean isGranted(Acl acl, List<Permission> permission, List<Sid> sids, boolean administrativeMode) {
        return true;
      }
    }, aclAuthorizationStrategy());
  }

  @Bean
  public LookupStrategy lookupStrategy() {
    return new BasicLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy(), auditLogger());
  }  
  
  @Bean
  public AclAuthorizationStrategy aclAuthorizationStrategy() {
    return new AclAuthorizationStrategyImpl(new Role(Role.Roles.ROLE_SUPER_ADMIN.name()));
  }
  
  @Bean
  public AuditLogger auditLogger() {
    return new ConsoleAuditLogger();
  }
  
  @Autowired
  private CacheManager cacheManager;
  
  private MutableAclService aclService;
  
  @Autowired
  public void setMutableAclService(MutableAclService aclService) {
    this.aclService = aclService;
  }
  
  @Autowired
  private DataSource dataSource;
 
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

  public List<String> getPermissionList(Authentication authentication, ObjectIdentity oid) {
    List<Sid> sids = sidRetrievalStrategy.getSids(authentication);
    // Lookup only ACLs for SIDs we're interested in
    Acl acl = aclService.readAclById(oid, sids);
    List<AccessControlEntry> aces = acl.getEntries();
    List<String> permissionsList = new ArrayList();
    for (AccessControlEntry ace : aces) {
      permissionsList.add(ace.getPermission().getPattern());
    }
    return permissionsList;
  }

}
