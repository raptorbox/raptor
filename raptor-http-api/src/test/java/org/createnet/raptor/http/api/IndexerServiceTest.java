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

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.createnet.raptor.http.ApplicationConfig;
import org.createnet.raptor.http.service.ConfigurationService;
import org.createnet.raptor.http.service.IndexerService;
import org.createnet.raptor.http.service.TreeService;
import org.createnet.raptor.models.objects.ServiceObject;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class IndexerServiceTest {

    @Inject
    IndexerService indexer;

    @Inject
    ConfigurationService config;

    @Inject
    TreeService tree;

    public IndexerServiceTest() {

        ServiceLocatorFactory locatorFactory = ServiceLocatorFactory.getInstance();
        ServiceLocator serviceLocator = locatorFactory.create("ObjectApiTest");
        ServiceLocatorUtilities.bind(serviceLocator, new ApplicationConfig.AppBinder());

        serviceLocator.inject(this);

        URL resource = getClass().getClassLoader().getResource("config");
        File file = new File(resource.getPath());
        config.setConfigPath(file);

        config.getIndexer().elasticsearch.indices.source = Paths.get(
                file.getAbsolutePath(),
                config.getIndexer().elasticsearch.indices.source
        ).toFile().getAbsolutePath();

        indexer.reset();
        tree.reset();
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

    @Test
    public void testIndex() {

        ServiceObject obj = new ServiceObject();
        obj.name = "test1";

        indexer.indexObject(obj, true);

        Assert.assertNotNull(obj.id);
    }

    @Test
    public void testAddChild() {

        ServiceObject a = new ServiceObject("a");
        ServiceObject b = new ServiceObject("b");
        ServiceObject c = new ServiceObject("c");
        ServiceObject d = new ServiceObject("d");

        indexer.saveObjects(Arrays.asList(a, b, c, d), null);

        tree.setChildren(a, Arrays.asList(b, c));
        List<ServiceObject> a_objs = tree.getChildren(a);
        
        Assert.assertEquals(2, a_objs.size());
        Assert.assertEquals(1, a_objs.stream().filter(o -> o.id.equals("b")).collect(Collectors.toList()).size());
        
        tree.setChildren(c, Arrays.asList(d));

        List<ServiceObject> c_objs = tree.getChildren(c);

        Assert.assertEquals(1, c_objs.size());
        Assert.assertEquals("d", c_objs.get(0).id);

    }
}
