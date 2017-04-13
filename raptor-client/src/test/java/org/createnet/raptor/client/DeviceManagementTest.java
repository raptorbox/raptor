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
package org.createnet.raptor.client;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import static org.createnet.raptor.client.DataStreamTest.raptor;
import org.createnet.raptor.indexer.query.impl.es.ObjectQuery;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.Stream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public class DeviceManagementTest {
    
    final Logger log = LoggerFactory.getLogger(DeviceManagementTest.class);
    
    public static Raptor raptor;

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }


    @Before
    public void setUp() {
        raptor = Utils.getRaptor();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void list() {
        log.debug("List devices");
        List<Device> list = raptor.Device.list();
        log.debug("found {} devices", list.size());
        assertNotNull(list);
    }

    @Test
    public void create() {

        Device dev = new Device();
        dev.name = "test create";

        dev.validate();
        raptor.Device.create(dev);
        
        log.debug("Device created {}", dev.id);
        assertNotNull(dev.id);
    }

    @Test
    public void updateStream()  {

        Device dev = new Device();
        dev.name = "test update";
        Stream s = dev.addStream("position", "location", "geo_point");
        dev.validate();
        raptor.Device.create(dev);
        
        log.debug("Device created {}", dev.id);
        
        s.addChannel("speed", "number");
        s.addChannel("color", "string");

        s.validate();

        log.debug("Added channels");
        
        Utils.waitFor(2500);
        raptor.Device.update(dev);

        log.debug("Updated, fetch list");
        
        Utils.waitFor(3500);
        List<Device> list = raptor.Device.list();

        log.debug("found {} devices", list.size());
        
        Device dev1 = list.stream().filter(d -> d.id.equals(dev.id)).findFirst().get();
        
        
        assertNotNull(dev1);
        assertTrue(dev1.streams.size() == dev.streams.size());
        
        log.debug("position channels {} == {}", dev1.streams.get("position").channels.size(), dev.streams.get("position").channels.size());
        assertTrue(dev1.streams.get("position").channels.size() == dev.streams.get("position").channels.size());
        assertTrue(dev1.streams.get("position").channels.containsKey("color"));
        
    }

    @Test
    public void update()  {
        
        Device dev = new Device();
        dev.name = "modified device";
        dev.validate();
        raptor.Device.create(dev);

        Utils.waitFor(2500);
        raptor.Device.update(dev);

        Utils.waitFor(2500);
        List<Device> list = raptor.Device.list();

        Device dev1 = list.stream().filter(d -> d.id.equals(dev.id)).findFirst().get();
        assertNotNull(dev1);

        assertEquals(dev1.name, dev.name);
    }

    @Test
    public void load()  {

        Device dev = new Device();
        dev.name = "test load";
        dev.validate();
        raptor.Device.create(dev);

        Utils.waitFor(3500);
        Device dev1 = raptor.Device.load(dev.id);

        assertTrue(dev1.name.equals(dev.name));
    }

    @Test
    public void searchByName()  {
        
        for (int i = 0; i < 3; i++) {
            log.debug("Create device {}", i);
            Device dev1 = new Device();
            dev1.name = "test-search "+ i;
            raptor.Device.create(dev1);
        }
        
        Utils.waitFor(2500);
        

        ObjectQuery q = new ObjectQuery();
        q.name = "test-search 1";
        log.debug("Searching for {}", q.toJSON().toString());
        List<Device> results = raptor.Device.search(q);
        
        log.debug("Results found {}", results.stream().map(d -> d.name).collect(Collectors.toList()));
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertEquals(results.get(0).name, q.name);

    }
   
    @Test
    public void searchByCustomFields()  {
        
        for (int i = 0; i < 3; i++) {
            log.debug("Create device d{}", i);
            Device dev1 = new Device();
            dev1.name = "d"+ i;
            dev1.customFields.put("model", System.currentTimeMillis());
            String v = i%2 == 0 ? "A" : "B";
            dev1.customFields.put("version", v);
            log.debug("Create device with version {}", v);
            raptor.Device.create(dev1);
        }
        
        Utils.waitFor(2500);
        
        String testVersion = "A";
        ObjectQuery q = new ObjectQuery();
        q.customFields.put("version", testVersion);
        log.debug("Searching for {}", q.toJSON().toString());
        
        List<Device> results = raptor.Device.search(q);
        
        log.debug("Results found {}", results.stream().map(d -> d.name).collect(Collectors.toList()));
        assertNotNull(results);
//        assertTrue(results.size() > 0);
//        assertTrue(results.get(0).customFields.get("version").equals(testVersion));

    }
    
}
