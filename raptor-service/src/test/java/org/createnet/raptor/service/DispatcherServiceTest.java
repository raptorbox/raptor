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
package org.createnet.raptor.service;

import java.io.File;
import java.net.URL;
import javax.inject.Inject;
import org.createnet.raptor.service.tools.ConfigurationService;
import org.createnet.raptor.service.tools.DispatcherService;
import org.createnet.raptor.service.tools.TreeService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class DispatcherServiceTest {

    @Inject
    DispatcherService dispatcher;

    @Inject
    ConfigurationService config;

    @Inject
    TreeService tree;

    public DispatcherServiceTest() {
        
        RaptorService.inject(this);

        URL resource = getClass().getClassLoader().getResource("config");
        File file = new File(resource.getPath());
        config.setConfigPath(file);

        dispatcher.reset();
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
    }

}
