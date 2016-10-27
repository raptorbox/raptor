/*
 * Copyright 2016 Luca Capra <lcapra@create-net.org>.
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
package org.createnet.raptor.auth.cache.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.auth.cache.AbstractCache;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class EHCache extends AbstractCache {

    private final Logger logger = LoggerFactory.getLogger(EHCache.class);

    final public static ObjectMapper mapper = new ObjectMapper();

    CacheManager cacheManager;
    private Cache<String, Boolean> authorizationCache;
    private Cache<String, String> authenticationCache;

    public CacheManager getManager() {
        if (cacheManager == null) {
            cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
        }
        return cacheManager;
    }

    public Cache<String, String> getAuthenticationCache() {
        if (authenticationCache == null) {
            authenticationCache = getManager().createCache(
                    "authenticationCache",
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(
                            String.class,
                            String.class,
                            ResourcePoolsBuilder.newResourcePoolsBuilder()
                                    .heap(this.configuration.ehcache.authentication.heapSize, EntryUnit.ENTRIES)
                                    .offheap(this.configuration.ehcache.authentication.inMemorySize, MemoryUnit.MB))
                            .withExpiry(Expirations.timeToLiveExpiration(Duration.of(this.configuration.ehcache.authentication.duration, TimeUnit.SECONDS)))
            );
        }
        return authenticationCache;
    }

    public Cache<String, Boolean> getAuthorizationCache() {
        if (authorizationCache == null) {
            authorizationCache = getManager().createCache(
                    "authorizationCache",
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(
                            String.class,
                            Boolean.class,
                            ResourcePoolsBuilder.newResourcePoolsBuilder()
                                    .heap(this.configuration.ehcache.authorization.heapSize, EntryUnit.ENTRIES)
                                    .offheap(this.configuration.ehcache.authorization.inMemorySize, MemoryUnit.MB))
                            .withExpiry(Expirations.timeToLiveExpiration(Duration.of(this.configuration.ehcache.authorization.duration, TimeUnit.SECONDS)))
            );
        }
        return authorizationCache;
    }

    @Override
    public void clear() {

        authorizationCache.clear();
        authenticationCache.clear();

        if (cacheManager != null) {
            cacheManager.close();
            cacheManager = null;
        }
    }

    @Override
    public Boolean get(String userId, String id, Authorization.Permission op) {
        if (id == null) {
            id = "_";
        }
        String key = userId + id + op.name();
        if (getAuthorizationCache().containsKey(key)) {
            return getAuthorizationCache().get(key);
        }
        return null;
    }

    @Override
    public void set(Authentication.UserInfo user) {
        String value;
        try {
            value = mapper.writeValueAsString(user);
        } catch (JsonProcessingException ex) {
            throw new RaptorComponent.ParserException(ex);
        }
        getAuthenticationCache().put(user.getAccessToken(), value);
    }

    @Override
    public void set(String userId, String id, Authorization.Permission op, boolean result) {
        String key = userId + id + op.toString();
        getAuthorizationCache().put(key, result);
    }

    @Override
    public Authentication.UserInfo get(String accessToken) {
        String cache = getAuthenticationCache().get(accessToken);
        Authentication.UserInfo value;
        try {
            return mapper.readValue(cache, Authentication.UserInfo.class);
        } catch (IOException ex) {
            throw new RaptorComponent.ParserException(ex);
        }
    }

}
