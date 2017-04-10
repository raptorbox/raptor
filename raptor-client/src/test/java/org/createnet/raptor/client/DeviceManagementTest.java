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

import java.util.List;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.Stream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public class DeviceManagementTest {
    
    public static Raptor raptor;
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        raptor = new Raptor("http://raptor.local", "admin", "admin");
        raptor.Auth.login();
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void list() {
        List<Device> list = raptor.Device.list();
        assertNotNull(list);
    }

    @Test
    public void create() {
        
        Device dev = new Device();
        
        dev.name = "test create";
        
        dev.validate();
        raptor.Device.create(dev);
        
        assertNotNull(dev.id);
    }

    @Test
    public void update() throws InterruptedException {
        
        Device dev = new Device();
        
        dev.name = "test update";
        
        dev.validate();
        raptor.Device.create(dev);
        
        Stream s = dev.addStream("position", "location", "geo_point");
        
        s.addChannel("speed", "number");
        s.addChannel("color", "string");
        
        s.validate();
        
        Thread.sleep(2500);
        raptor.Device.update(dev);
        
        assertNotNull(dev.getStream("position").channels.size() == s.channels.size());
    }

    @Test
    public void load() throws InterruptedException {
        
        Device dev = new Device();
        dev.name = "test load";
        dev.validate();
        raptor.Device.create(dev);
        
        Thread.sleep(3500);
        Device dev1 = raptor.Device.load(dev.id);
        
        assertNotNull(dev1.name.equals(dev.name));
    }    
    
}
