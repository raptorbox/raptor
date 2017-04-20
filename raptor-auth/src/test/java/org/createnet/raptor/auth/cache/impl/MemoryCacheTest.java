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
package org.createnet.raptor.auth.cache.impl;

import java.util.Map;
import org.createnet.raptor.auth.AuthConfiguration;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.models.acl.Permissions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class MemoryCacheTest {
  
  MemoryCache instance;
  
  public MemoryCacheTest() {
  }
  
  @BeforeClass
  public static void setUpClass() {
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() {
    
    AuthConfiguration config = new AuthConfiguration();
    
    config.cache = AuthConfiguration.Cache.memory.toString();
    config.type = AuthConfiguration.Type.allow_all.toString();
    
    instance =  new MemoryCache();
    instance.initialize(config);
  }
  
  @After
  public void tearDown() {
    instance.clear();
    instance = null;
  }

  @Test
  public void testScheduledRemoval() throws Exception {
  
    String userId = "user";
    String objectId = "obj";
    Permissions perm = Permissions.create;
    
    MemoryCache.CachedItem.defaultTTL = 100;
    
    boolean value = true;
    
    instance.set(userId, objectId, perm, value);
    
    assertEquals(value, instance.get(userId, objectId, perm));
    
    Thread.sleep(MemoryCache.CachedItem.defaultTTL + 1000);
    
    
    assertNotEquals(value, instance.get(userId, objectId, perm));
    
  }

}
