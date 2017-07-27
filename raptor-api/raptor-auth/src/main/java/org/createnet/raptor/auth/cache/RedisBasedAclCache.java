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
package org.createnet.raptor.auth.cache;

import com.mysema.commons.lang.Assert;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheElement;
import org.springframework.data.redis.cache.RedisCacheKey;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclImpl;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.util.FieldUtils;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class RedisBasedAclCache implements AclCache {
    
    final protected Logger logger = LoggerFactory.getLogger(RedisBasedAclCache.class);
    
    // ~ Instance fields
    // ================================================================================================

    private final RedisCache cache;
    private PermissionGrantingStrategy permissionGrantingStrategy;
    private AclAuthorizationStrategy aclAuthorizationStrategy;

    // ~ Constructors
    // ===================================================================================================
    public RedisBasedAclCache(RedisCache cache,
            PermissionGrantingStrategy permissionGrantingStrategy,
            AclAuthorizationStrategy aclAuthorizationStrategy) {
        Assert.notNull(cache, "Cache required");
        Assert.notNull(permissionGrantingStrategy, "PermissionGrantingStrategy required");
        Assert.notNull(aclAuthorizationStrategy, "AclAuthorizationStrategy required");
        this.cache = cache;
        this.permissionGrantingStrategy = permissionGrantingStrategy;
        this.aclAuthorizationStrategy = aclAuthorizationStrategy;
    }

    // ~ Methods
    // ========================================================================================================
    
    protected RedisCacheKey getRedisCacheKey(Object val) {
        JdkSerializationRedisSerializer serializer = new JdkSerializationRedisSerializer();
        RedisCacheKey key = new RedisCacheKey(val);
        key.setSerializer(serializer);
        return key;
    }
    
    /**
     *
     * @param pk
     */
    @Override
    public void evictFromCache(Serializable pk) {
        Assert.notNull(pk, "Primary key (identifier) required");

        MutableAcl acl = getFromCache(pk);

        if (acl != null) {
            cache.evict(acl.getId());
            cache.evict(acl.getObjectIdentity());
        }
    }

    @Override
    public void evictFromCache(ObjectIdentity objectIdentity) {
        Assert.notNull(objectIdentity, "ObjectIdentity required");

        MutableAcl acl = getFromCache(objectIdentity);

        if (acl != null) {
            cache.evict(acl.getId());
            cache.evict(acl.getObjectIdentity());
        }
    }

    @Override
    public MutableAcl getFromCache(ObjectIdentity objectIdentity) {
        Assert.notNull(objectIdentity, "ObjectIdentity required");

        RedisCacheElement element = null;

        try {
            element = (RedisCacheElement) cache.get(getRedisCacheKey(objectIdentity));
        } catch (Exception e) {
            logger.warn("Error getting ACL cache: %s", e.getMessage());
        }

        if (element == null) {
            return null;
        }

        return initializeTransientFields((MutableAcl) element.get());
    }

    @Override
    public MutableAcl getFromCache(Serializable pk) {
        Assert.notNull(pk, "Primary key (identifier) required");

        RedisCacheElement element = null;       
        
        try {
            element = (RedisCacheElement) cache.get(getRedisCacheKey(pk));
        } catch (Exception e) {
            logger.warn("Error getting ACL cache: %s", e.getMessage());
        }

        if (element == null) {
            return null;
        }

        return initializeTransientFields((MutableAcl) element.get());
    }

    @Override
    public void putInCache(MutableAcl acl) {
        Assert.notNull(acl, "Acl required");
        Assert.notNull(acl.getObjectIdentity(), "ObjectIdentity required");
        Assert.notNull(acl.getId(), "ID required");

        if (this.aclAuthorizationStrategy == null) {
            if (acl instanceof AclImpl) {
                this.aclAuthorizationStrategy = (AclAuthorizationStrategy) FieldUtils
                        .getProtectedFieldValue("aclAuthorizationStrategy", acl);
                this.permissionGrantingStrategy = (PermissionGrantingStrategy) FieldUtils
                        .getProtectedFieldValue("permissionGrantingStrategy", acl);
            }
        }

        if ((acl.getParentAcl() != null) && (acl.getParentAcl() instanceof MutableAcl)) {
            putInCache((MutableAcl) acl.getParentAcl());
        }
        
        cache.put(new RedisCacheElement(getRedisCacheKey(acl.getObjectIdentity()), acl));
        cache.put(new RedisCacheElement(getRedisCacheKey(acl.getId()), acl));
    }

    private MutableAcl initializeTransientFields(MutableAcl value) {
        if (value instanceof AclImpl) {
            FieldUtils.setProtectedFieldValue("aclAuthorizationStrategy", value,
                    this.aclAuthorizationStrategy);
            FieldUtils.setProtectedFieldValue("permissionGrantingStrategy", value,
                    this.permissionGrantingStrategy);
        }

        if (value.getParentAcl() != null) {
            initializeTransientFields((MutableAcl) value.getParentAcl());
        }
        return value;
    }

    @Override
    public void clearCache() {
        cache.clear();
    }
}
