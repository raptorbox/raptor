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
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.createnet.raptor.indexer.impl.ElasticSearchIndexer;
import org.createnet.raptor.service.tools.CacheService;
import org.createnet.raptor.service.tools.ConfigurationService;
import org.createnet.raptor.service.tools.IndexerService;
import org.createnet.raptor.service.tools.TreeService;
import org.createnet.raptor.models.data.IRecord;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.objects.Channel;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.Stream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class IndexerServiceTest {

    @Inject
    IndexerService indexer;

    @Inject
    CacheService cache;

    @Inject
    ConfigurationService config;

    @Inject
    TreeService tree;

    public IndexerServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        
        RaptorService.inject(this);

        URL resource = getClass().getClassLoader().getResource("config");
        File file = new File(resource.getPath());
        config.setConfigPath(file);

        config.getIndexer().elasticsearch.indices.source = Paths.get(
                file.getAbsolutePath(),
                config.getIndexer().elasticsearch.indices.source
        ).toFile().getAbsolutePath();
        
        cache.reset();
        indexer.reset();
        tree.reset();
        
        indexer.getIndexer().open();
        indexer.getIndexer().setup(true);
    }

    @After
    public void tearDown() {
    }

    protected String rndName(String prefix) {
        return prefix + "_" + UUID.randomUUID();
    }

    @Test
    public void testIndex() {

        Device obj = new Device();
        obj.name = "test1";

        indexer.saveObject(obj, true);

        Assert.assertNotNull(obj.id);
    }

    @Test
    public void testSetChild() {

        Device a = new Device(rndName("a"));

        String nameB = rndName("b");
        Device b = new Device(nameB);

        Device c = new Device(rndName("c"));

        String nameD = rndName("b");
        Device d = new Device(nameD);

        indexer.saveObjects(Arrays.asList(a, b, c, d), null);

        tree.setChildren(a, Arrays.asList(b, c));
        List<Device> a_objs = tree.getChildren(a);

        Assert.assertEquals(2, a_objs.size());
        Assert.assertEquals(1, a_objs.stream().filter(o -> o.id.equals(nameB)).collect(Collectors.toList()).size());

        tree.setChildren(c, Arrays.asList(d));

        List<Device> c_objs = tree.getChildren(c);

        Assert.assertEquals(1, c_objs.size());
        Assert.assertEquals(nameD, c_objs.get(0).id);

    }

    protected List<Device> addChild(String pid, String cid) {

        Device a = new Device(pid);
        Device f = new Device(cid);

        indexer.saveObjects(Arrays.asList(a, f));
        tree.addChildren(a, Arrays.asList(f));

        return tree.getChildren(a);
    }

    @Test
    public void testPath() throws InterruptedException {

        List<String> ids = Arrays.asList("x1", "x2", "x3", "x4", "x5", "x6");
        List<Device> objects = ids.stream().sequential()
                .map(s -> new Device(s))
                .collect(Collectors.toList());

        List<Device> list = null;
        indexer.saveObjects(objects, null);

        for (int i = 0; i < objects.size(); i++) {

            Device curr = objects.get(i);
            Device prev = null;
            if (i > 0) {
                prev = objects.get(i - 1);
                list = tree.addChildren(prev, Arrays.asList(curr));
            }

            Thread.sleep(1000);
        }

        String expectedPath = String.join("/", ids);

        Device x6 = list.get(0);
        String actualPath = x6.path() + "/" + x6.id;

        Assert.assertNotNull(list);
        Assert.assertEquals(
                expectedPath,
                actualPath
        );

    }

    @Test
    public void testAddChild() {

        String id = rndName("f");
        List<Device> children = addChild(rndName("add"), id);

        Assert.assertEquals(
                1,
                children.stream()
                        .filter(c -> c.id.equals(id))
                        .collect(Collectors.toList())
                        .size()
        );

    }

    @Test
    public void testRemoveChild() {

        String pid = rndName("rm");
        String id = rndName("g");

        List<Device> children1 = addChild(pid, id);
        List<Device> children2 = tree.removeChildren(pid, Arrays.asList(id));

        Assert.assertEquals(
                0,
                children2.stream()
                        .filter(c -> c.id.equals(id))
                        .collect(Collectors.toList())
                        .size()
        );

    }
    
    @Test
    public void testRemoveData() {
        
        Device obj = new Device(rndName("test remove"));
        
        Channel c = new Channel();
        c.name = "foochannel";
        c.type = "string";

        Stream stream = new Stream();
        stream.name = "test";
        stream.channels.put("string", c);
        stream.setDevice(obj);
        
        List<RecordSet> records = new ArrayList();
        for (int i = 0; i < 5000; i++) {
            
            String val = "foo bar #" + Instant.now().toEpochMilli();
            
            RecordSet rs = new RecordSet(stream);
            IRecord rval = RecordSet.createRecord(stream, c.name, val);
            rs.getRecords().put(c.name, rval);

            records.add(rs);
        }

        indexer.saveData(records);
        indexer.deleteData(stream);
    }
}
