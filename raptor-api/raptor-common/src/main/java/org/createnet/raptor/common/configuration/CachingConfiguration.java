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
package org.createnet.raptor.common.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Configuration
@EnableCaching
public class CachingConfiguration {
//
//    final protected int expiresSeconds = 30;
//
//    @Value("${spring.redis.hostname}")
//    private String redisHostName;
//
//    @Value("${spring.redis.port:6379}")
//    private int redisPort;
//
//    @Bean
//    JedisConnectionFactory jedisConnectionFactory() {
//        JedisConnectionFactory factory = new JedisConnectionFactory();
//        factory.setHostName(redisHostName);
//        factory.setPort(redisPort);
//        factory.setUsePool(true);
//        return factory;
//    }
//
//    @Bean
//    RedisTemplate<Object, Object> redisTemplate() {
//
//        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate();
//        
//        redisTemplate.setConnectionFactory(jedisConnectionFactory());
//        redisTemplate.setDefaultSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
//        
//        return redisTemplate;
//    }
//
//    @Bean
//    RedisCacheManager cacheManager() {
//        
//        RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate());
//        redisCacheManager.setDefaultExpiration(expiresSeconds);       
//
//        return redisCacheManager;
//    }
}
