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
package org.createnet.raptor.sdk.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.Utils;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.models.query.DeviceQuery;
import org.createnet.raptor.sdk.PageResponse;
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
public class InventoryTest {

    final Logger log = LoggerFactory.getLogger(InventoryTest.class);

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
    public void list() {
        Raptor raptor = Utils.createNewAdminInstance();
        log.debug("List devices");
        PageResponse<Device> list = raptor.Inventory().list();
        log.debug("found {} devices", list.getContent().size());
        assertNotNull(list);
    }

    @Test
    public void create() {

        Raptor raptor = Utils.createNewAdminInstance();

        Device dev = new Device();
        dev.name("test create")
                .description("info about");

        dev.properties().put("active", true);
        dev.properties().put("version", 15L);

        dev.validate();
        raptor.Inventory().create(dev);

        log.debug("Device created {}", dev.id());
        assertNotNull(dev.id());
    }

    @Test
    public void updateStream() {

        Raptor raptor = Utils.createNewAdminInstance();

        Device dev = new Device();
        dev.name("test update");
        Stream s = dev.addStream("position", "location", "string");
        dev.validate();
        raptor.Inventory().create(dev);

        log.debug("Device created {}", dev.id());

        s.addChannel("speed", "number");
        s.addChannel("color", "string");

        s.validate();

        log.debug("Added channels");

        raptor.Inventory().update(dev);

        log.debug("Updated, fetch list");

        Device dev1 = raptor.Inventory().load(dev.id());

        assertNotNull(dev1);
        assertTrue(dev1.streams().size() == dev.streams().size());

        log.debug("position channels {} == {}", dev1.streams().get("position").channels().size(), dev.streams().get("position").channels().size());
        assertTrue(dev1.streams().get("position").channels().size() == dev.streams().get("position").channels().size());
        assertTrue(dev1.streams().get("position").channels().containsKey("color"));

    }

    @Test
    public void update() {
        
        Raptor raptor = Utils.createNewAdminInstance();
        
        Device dev = new Device();
        dev.name("new device");
        dev.validate();

        raptor.Inventory().create(dev);

        dev.name("updated device");
        dev.properties().put("foo", "bar");

        raptor.Inventory().update(dev);

        List<Device> list = raptor.Inventory().list().getContent();
        
        // first by sorting asc
        assertEquals(list.get(0).name(), dev.name());
        
        Optional<Device> odev1 = list.stream().filter(d -> d.id().equals(dev.id())).findFirst();
        
        assertTrue(odev1.isPresent());
        
        Device dev1 = odev1.get();
        assertEquals(dev1.name(), dev.name());
        assertEquals(dev1.properties().get("foo"), dev.properties().get("foo"));
    }

    @Test
    public void load() {
        Raptor raptor = Utils.createNewAdminInstance();
        Device dev = new Device();
        dev.name("test load");
        dev.validate();
        raptor.Inventory().create(dev);
        Device dev1 = raptor.Inventory().load(dev.id());
        assertTrue(dev1.name().equals(dev.name()));
    }
    
    @Test
    public void searchByDomainId() {
    	String domainID = "4166aec1-e491-484b-a217-e6cf5bf497b9";
        Raptor raptor = Utils.createNewAdminInstance();
        for (int i = 0; i < 3; i++) {
            log.debug("Create device {}", i);
            Device dev1 = new Device();
            dev1.name("test-search " + i);
            dev1.domain(domainID);
            dev1.properties().put("version", i);
            dev1.properties().put("active", i % 2 == 0);
            raptor.Inventory().create(dev1);
        }

        DeviceQuery q = new DeviceQuery();
        q.domain.equals(domainID);
        log.debug("Searching for {}", q.toJSON().toString());
        List<Device> results = raptor.Inventory().search(q).getContent();

        log.debug("Results found {}", results.stream().map(d -> d.name()).collect(Collectors.toList()));
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertTrue(results.get(0).domain().equals(domainID));

    }

    @Test
    public void searchByName() {

        Raptor raptor = Utils.createNewAdminInstance();
        for (int i = 0; i < 3; i++) {
            log.debug("Create device {}", i);
            Device dev1 = new Device();
            dev1.name("test-search " + i);
            dev1.properties().put("version", i);
            dev1.properties().put("active", i % 2 == 0);
            raptor.Inventory().create(dev1);
        }

        DeviceQuery q = new DeviceQuery();
        q.name.contains("test-search");
        log.debug("Searching for {}", q.toJSON().toString());
        List<Device> results = raptor.Inventory().search(q).getContent();

        log.debug("Results found {}", results.stream().map(d -> d.name()).collect(Collectors.toList()));
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertTrue(results.get(0).name().contains(q.name.getContains()));

    }

    @Test
    public void searchByProperties() {

        Raptor raptor = Utils.createNewAdminInstance();
        for (int i = 0; i < 3; i++) {
            log.debug("Create device d{}", i);
            Device dev1 = new Device();
            dev1.name("d" + i);
            dev1.properties().put("model", System.currentTimeMillis());
            String v = i % 2 == 0 ? "A" : "B";
            dev1.properties().put("version", v);
            log.debug("Create device with version {}", v);
            raptor.Inventory().create(dev1);
        }

        String testVersion = "A";
        DeviceQuery q = new DeviceQuery();
        q.properties.has("version", testVersion);
        log.debug("Searching for {}", q.toJSON().toString());

        List<Device> results = raptor.Inventory().search(q).getContent();

        log.debug("Results found {}", results.stream().map(d -> d.name()).collect(Collectors.toList()));
        assertNotNull(results);

    }

    @Test
    public void searchByUserId() {

        Raptor raptor = Utils.createNewAdminInstance();

        Raptor r = Utils.createNewAdminInstance();

        log.debug("Create device by user {}", r.Auth().getUser().getUsername());
        Device dev1 = new Device();
        dev1.name("test dev with prop");
        r.Inventory().create(dev1);

        String userId = r.Auth().getUser().getId();

        DeviceQuery q = new DeviceQuery();
        q.userId(userId);

        log.debug("Searching for {}", q.toJSON().toString());

        List<Device> results = raptor.Inventory().search(q).getContent();

        log.debug("Results found {}", results.stream().map(d -> d.name()).collect(Collectors.toList()));
        assertEquals(1, results.size());

    }

    @Test
    public void searchByUserIdAndProperties() {

        Raptor raptor = Utils.createNewAdminInstance();
        Raptor r = Utils.createNewAdminInstance();

        log.debug("Create device by user {}", r.Auth().getUser().getUsername());
        Device dev1 = new Device();
        dev1.name("test dev");
        dev1.properties().put("test", true);

        r.Inventory().create(dev1);

        String userId = r.Auth().getUser().getId();

        DeviceQuery q = new DeviceQuery();
        q.userId(userId);
        q.properties.has("test", true);

        log.debug("Searching for {}", q.toJSON().toString());

        List<Device> results = raptor.Inventory().search(q).getContent();

        log.debug("Results found {}", results.stream().map(d -> d.name()).collect(Collectors.toList()));
        assertEquals(1, results.size());

    }

    @Test
    public void changeUserIdForDevice() {

        Raptor raptor = Utils.createNewAdminInstance();
        Raptor r = Utils.createNewAdminInstance();
        Raptor r2 = Utils.createNewAdminInstance();

        log.debug("Create device by user {}", r.Auth().getUser().getUsername());
        Device dev1 = new Device();
        dev1.name("test dev");
        dev1.properties().put("test", true);

        r.Inventory().create(dev1);

        String userId = r.Auth().getUser().getId();
        String newUser = r2.Auth().getUser().getId();

        DeviceQuery q = new DeviceQuery();
        q.userId(userId);
        q.properties.has("test", true);

        log.debug("Searching for {}", q.toJSON().toString());

        List<Device> results = raptor.Inventory().search(q).getContent();

        log.debug("Results found {}", results.stream().map(d -> d.name()).collect(Collectors.toList()));

        assertEquals(1, results.size());

        Device d = results.get(0).userId(newUser);
        d.name("user id changed");
        Device dd = raptor.Inventory().update(d);
        dd.toString();

        DeviceQuery q1 = new DeviceQuery();
        q1.userId(newUser);
        q1.properties.has("test", true);

        log.debug("Searching for {}", q1.toJSON().toString());

        List<Device> newResults = raptor.Inventory().search(q1).getContent();

        log.debug("Results found {}", newResults.stream().map(d1 -> d1.toString()).collect(Collectors.toList()));
        assertEquals(1, newResults.size());

    }

}
