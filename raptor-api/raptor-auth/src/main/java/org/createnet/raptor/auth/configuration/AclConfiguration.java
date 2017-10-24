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
package org.createnet.raptor.auth.configuration;

import javax.sql.DataSource;
import org.createnet.raptor.models.acl.RaptorPermission;
import org.createnet.raptor.models.auth.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionFactory;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.domain.EhCacheBasedAclCache;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Configuration
public class AclConfiguration {

    @Autowired
    private DataSource dataSource;

    @Autowired
    CacheManager cacheManager;

    @Value("${spring.datasource.lastInsertQuery}")
    private String lastInsertQuery;

    @Bean
    public LookupStrategy lookupStrategy() {
        BasicLookupStrategy ls = new BasicLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy(), auditLogger());
        ls.setPermissionFactory(new DefaultPermissionFactory(RaptorPermission.class));
        return ls;
    }

    @Bean
    public AclAuthorizationStrategy aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(new Role(Role.Roles.super_admin.name()));
    }

    @Bean
    public EhCacheBasedAclCache aclCache() {
        return new EhCacheBasedAclCache(aclEhCacheFactoryBean().getObject(), permissionGrantingStrategy(), aclAuthorizationStrategy());
    }

    @Bean
    public EhCacheFactoryBean aclEhCacheFactoryBean() {
        EhCacheFactoryBean ehCacheFactoryBean = new EhCacheFactoryBean();
        ehCacheFactoryBean.setCacheManager(cacheManagerFactory().getObject());
        ehCacheFactoryBean.setCacheName("aclCache");
        return ehCacheFactoryBean;
    }

    @Bean
    public EhCacheManagerFactoryBean cacheManagerFactory() {
        EhCacheManagerFactoryBean m = new EhCacheManagerFactoryBean();
        m.setShared(true);
        return m;
    }
    
    @Bean
    public DefaultPermissionGrantingStrategy permissionGrantingStrategy() {
        DefaultPermissionGrantingStrategy pgs = new DefaultPermissionGrantingStrategy(auditLogger());
        return pgs;
    }

    /**
     * @return @TODO Add additional support for @setSidIdentityQuery
     */
    @Bean
    public JdbcMutableAclService aclService() {
        JdbcMutableAclService service = new JdbcMutableAclService(dataSource, lookupStrategy(), aclCache());
        service.setSidIdentityQuery(lastInsertQuery);
        service.setClassIdentityQuery(lastInsertQuery);
        return service;
    }

    @Bean
    public AuditLogger auditLogger() {
        return new ConsoleAuditLogger();
    }

}
