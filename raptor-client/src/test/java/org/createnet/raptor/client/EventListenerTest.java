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

import org.createnet.raptor.client.api.DeviceClient.DeviceCallback;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.createnet.raptor.models.payload.ObjectPayload;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public class EventListenerTest {

    final Logger log = LoggerFactory.getLogger(EventListenerTest.class);

    public static Raptor raptor;
    public static Device device;

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

        raptor = Utils.getRaptor();

        Device d = new Device();
        d.name = "listener test " + System.currentTimeMillis();
        
        d.addAction("switch");
        d.addAction("dimming");
        d.addAction("battery");
        
        d.addStream("test", "string", "string");
        
        Assert.assertEquals(3, d.getActions().size());
        Assert.assertEquals(1, d.getStreams().size());

        log.debug("Creating {} device", d.name);

        device = d;
    }

    @After
    public void tearDown() {
    }

    @Test
    public void watchDeviceEvents()  {

        log.debug("watch device events");
        
        Device dev = Utils.createDevice(device);
//        raptor.Device.subscribe(dev, new DeviceCallback() {
//            @Override
//            public void callback(Device obj, ObjectPayload message) {
//                
//            }
//        });
        
        dev.addStream("test2", "foo", "boolean");
        dev.addAction("sleep");
        
        dev = raptor.Device.update(dev);
        Utils.waitFor(2500);
        
        Stream stream = dev.getStream("test2");
        RecordSet record = new RecordSet(stream);
        record.createRecord("foo", true);
        
        raptor.Stream.push(stream, record);

    }

}
