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
package org.createnet.raptor.http.api;

import org.createnet.raptor.http.ApplicationConfig;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ObjectApiTest {
  
  public ObjectApiTest() {
  }
  
  @BeforeClass
  public static void setUpClass() {
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() {
    
    ServiceLocatorFactory locatorFactory = ServiceLocatorFactory.getInstance();
    ServiceLocator serviceLocator = locatorFactory.create("ObjectApiTest");
    ServiceLocatorUtilities.bind(serviceLocator, new ApplicationConfig.AppBinder());

    serviceLocator.inject(this);

  }
  
  @After
  public void tearDown() {
  }
  
  // TBD
  
}
