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
package org.createnet.raptor.sdk.events;

import java.time.Instant;
import java.util.Date;
import org.createnet.raptor.models.acl.PermissionUtil;
import org.createnet.raptor.models.acl.Permissions;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.Utils;
import org.createnet.raptor.sdk.events.callback.ActionCallback;
import org.createnet.raptor.sdk.events.callback.DataCallback;
import org.createnet.raptor.sdk.events.callback.DeviceCallback;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.models.payload.ActionPayload;
import org.createnet.raptor.models.payload.DevicePayload;
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
    }

    private Device newDevice(String name) {
        Device d = new Device();
        d.name = name;

        d.addAction("switch");
        d.addAction("dimming");
        d.addAction("battery");

        d.addStream("test", "string", "string");
        d.addStream("test2", "foo", "boolean");

        Assert.assertEquals(3, d.getActions().size());
        Assert.assertEquals(2, d.getStreams().size());

        log.debug("Creating {} device", d.name);

        return d;
    }

    @After
    public void tearDown() {
    }

    private void pushData(Device dev) {

        Stream stream = dev.getStream("test2");
        RecordSet record = new RecordSet(stream);
        record.createRecord("foo", true);

        raptor.Stream.push(stream, record);
        record.timestamp = new Date(Instant.now().toEpochMilli());
        Utils.waitFor(500);

        raptor.Stream.push(stream, record);
        record.timestamp = new Date(Instant.now().toEpochMilli());
        Utils.waitFor(500);

        raptor.Stream.push(stream, record);
        record.timestamp = new Date(Instant.now().toEpochMilli());
        Utils.waitFor(500);

        raptor.Stream.push(stream, record);
        record.timestamp = new Date(Instant.now().toEpochMilli());
        Utils.waitFor(500);
    }

    @Test
    public void watchDeviceEvents() {

        log.debug("watch data events");

        final Device dev = Utils.createDevice(newDevice("dev"));

        raptor.Device.subscribe(dev, new DeviceCallback() {
            @Override
            public void callback(Device obj, DevicePayload message) {
                log.debug("Device event received {}", message.toString());
                Assert.assertEquals(obj.id, dev.id);
            }
        });
        dev.addStream("test2", "foo", "boolean");
        dev.addAction("sleep");

        raptor.Device.update(dev);
        Utils.waitFor(1000);

    }

    @Test
    public void watchDeviceDataEvents() {

        log.debug("watch data events");

        Device dev = Utils.createDevice(newDevice("dev"));
        Device dev1 = Utils.createDevice(newDevice("dev1"));

        Utils.waitFor(1500);

        raptor.Device.subscribe(dev, new DataCallback() {
            @Override
            public void callback(Stream stream, RecordSet record) {
                log.debug("dev: Data received {}", record.toJson());
                Assert.assertTrue(record.objectId.equals(dev.getDevice().getId()));
                Assert.assertTrue(stream.name.equals("test2"));
            }
        });

        raptor.Device.subscribe(dev1, new DataCallback() {
            @Override
            public void callback(Stream stream, RecordSet record) {
                log.debug("dev1: Data received {}", record.toJson());
                Assert.assertTrue(record.objectId.equals(dev1.getDevice().getId()));
                Assert.assertTrue(stream.name.equals("test2"));
            }
        });

        pushData(dev);
        pushData(dev1);

        Utils.waitFor(1000);
    }

    @Test
    public void watchDeviceActionEvents() {

        log.debug("watch action events");

        Device dev = Utils.createDevice(newDevice("dev"));
        Utils.waitFor(1500);

        raptor.Device.subscribe(dev, new ActionCallback() {
            @Override
            public void callback(Action action, ActionPayload payload) {
                log.debug("dev: Data received  for {}: {}", payload.actionId, payload.data);
                Assert.assertTrue(action.name.equals("switch"));
                Assert.assertTrue(payload.data.equals("on"));
            }
        });

        Action action = dev.getAction("switch");
        raptor.Action.invoke(action, "on");

        Utils.waitFor(1000);
    }

    @Test
    public void subscribeWithToken() {

        log.debug("subscribe with permission token");

        Raptor r = Utils.createNewInstance();
        r.Auth.login();

        Token t = r.Admin.Token.create(new Token("test", "test"));

        r.Admin.Token.Permission.set(t, PermissionUtil.asList(Permissions.admin));

        Device dev = r.Device.create(newDevice("dev"));
        Utils.waitFor(1500);

        Raptor r2 = new Raptor(Utils.loadSettings().getProperty("url"), t);

        r2.Device.subscribe(dev, new ActionCallback() {
            @Override
            public void callback(Action action, ActionPayload payload) {
                log.debug("dev: Data received  for {}: {}", payload.actionId, payload.data);
                Assert.assertTrue(action.name.equals("switch"));
                Assert.assertTrue(payload.data.equals("on"));
            }
        });

        Action action = dev.getAction("switch");
        raptor.Action.invoke(action, "on");

        Utils.waitFor(1000);
    }

    @Test
    public void checkFailingSubscribePermission() {

        log.debug("subscribe with failing permissions");

        Raptor r = Utils.createNewInstance();
        r.Auth.login();

        Token t = r.Admin.Token.create(new Token("test", "test"));
        r.Admin.Token.Permission.set(t, PermissionUtil.asList(Permissions.subscribe));

        Device dev = r.Device.create(newDevice("dev"));
        Utils.waitFor(1500);

        Raptor r2 = new Raptor(Utils.loadSettings().getProperty("url"), t);
        
        try {
            r2.Device.subscribe(dev, new DataCallback() {
                @Override
                public void callback(Stream stream, RecordSet record) {
                    log.debug("Got data: {}", record.toJson());
                }
            });
        }
        catch(Exception e) {
            log.debug("Exception: {}", e.getMessage());
        }
        
        Stream stream = dev.getStream("test");

        RecordSet record = new RecordSet(stream);
        record.addRecord(RecordSet.createRecord(stream, "string", "test1"));
        
        raptor.Stream.push(record);

        Utils.waitFor(1000);
    }

}
