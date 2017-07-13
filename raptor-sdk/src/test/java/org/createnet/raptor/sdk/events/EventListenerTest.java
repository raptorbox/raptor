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
import org.createnet.raptor.models.payload.TreeNodePayload;
import org.createnet.raptor.models.tree.TreeNode;
import org.createnet.raptor.sdk.events.callback.TreeNodeCallback;
import org.createnet.raptor.sdk.events.callback.TreeNodeEventCallback;
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

    public static Device device;

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

    }

    private Device newDevice(String name) {
        Device d = new Device();
        d.name(name);

        d.addAction("switch");
        d.addAction("dimming");
        d.addAction("battery");

        d.addStream("test", "string", "string");
        d.addStream("test2", "foo", "boolean");

        Assert.assertEquals(3, d.actions().size());
        Assert.assertEquals(2, d.streams().size());

        log.debug("Creating {} device", d.name());

        return d;
    }

    @After
    public void tearDown() {
    }

    private void pushData(Device dev) {

        Raptor raptor = Utils.getRaptor();

        Stream stream = dev.stream("test2");
        RecordSet record = new RecordSet(stream);
        record.channel("foo", true);

        raptor.Stream().push(stream, record);
        record.timestamp = new Date(Instant.now().toEpochMilli());
        Utils.waitFor(500);

        raptor.Stream().push(stream, record);
        record.timestamp = new Date(Instant.now().toEpochMilli());
        Utils.waitFor(500);

        raptor.Stream().push(stream, record);
        record.timestamp = new Date(Instant.now().toEpochMilli());
        Utils.waitFor(500);

        raptor.Stream().push(stream, record);
        record.timestamp = new Date(Instant.now().toEpochMilli());
        Utils.waitFor(500);
    }

    @Test
    public void watchDeviceEvents() {

        Raptor raptor = Utils.getRaptor();

        log.debug("watch data events");

        final Device dev = Utils.createDevice(newDevice("dev"));

        raptor.Inventory().subscribe(dev, new DeviceCallback() {
            @Override
            public void callback(Device obj, DevicePayload message) {
                log.debug("Device event received {}", message.toString());
                Assert.assertEquals(obj.id(), dev.id());
            }
        });
        dev.addStream("test2", "foo", "boolean");
        dev.addAction("sleep");

        raptor.Inventory().update(dev);
        Utils.waitFor(1000);

    }

    @Test
    public void watchDeviceTreeEvents() {

        Raptor raptor = Utils.getRaptor();

        log.debug("watch device tree events");

        final Device dev = raptor.Inventory().create(newDevice("dev"));

        final TreeNode node = TreeNode.create("parent");
        raptor.Tree().create(node);
        raptor.Tree().add(node, dev);

        raptor.Tree().subscribe(node, new TreeNodeCallback() {
            @Override
            public void callback(TreeNode node, TreeNodePayload message) {
                log.debug("TreeNode event received {}", message.toString());
//                Assert.assertEquals(node.getId(), node.getId());
            }
        });

        dev.addStream("test2", "foo", "boolean");
        dev.addAction("sleep");

        raptor.Inventory().update(dev);
        Utils.waitFor(1000);

    }

    @Test
    public void watchDeviceDataEvents() {

        Raptor raptor = Utils.getRaptor();

        log.debug("watch data events");

        Device dev = Utils.createDevice(newDevice("dev"));
        Device dev1 = Utils.createDevice(newDevice("dev1"));

        Utils.waitFor(1500);

        raptor.Inventory().subscribe(dev, new DataCallback() {
            @Override
            public void callback(Stream stream, RecordSet record) {
                log.debug("dev: Data received {}", record.toJson());
                Assert.assertTrue(record.deviceId.equals(dev.getDevice().id()));
                Assert.assertTrue(stream.name.equals("test2"));
            }
        });

        raptor.Inventory().subscribe(dev1, new DataCallback() {
            @Override
            public void callback(Stream stream, RecordSet record) {
                log.debug("dev1: Data received {}", record.toJson());
                Assert.assertTrue(record.deviceId.equals(dev1.getDevice().id()));
                Assert.assertTrue(stream.name.equals("test2"));
            }
        });

        pushData(dev);
        pushData(dev1);

        Utils.waitFor(1000);
    }

    @Test
    public void watchDeviceActionEvents() {

        Raptor raptor = Utils.getRaptor();

        log.debug("watch action events");

        Device dev = Utils.createDevice(newDevice("dev"));
        Utils.waitFor(1500);

        raptor.Inventory().subscribe(dev, new ActionCallback() {
            @Override
            public void callback(Action action, ActionPayload payload) {
                log.debug("dev: Data received  for {}: {}", payload.actionId, payload.data);
                Assert.assertTrue(action.name.equals("switch"));
                Assert.assertTrue(payload.data.equals("on"));
            }
        });

        Action action = dev.action("switch");
        raptor.Action().invoke(action, "on");

        Utils.waitFor(1000);
    }

    @Test
    public void subscribeWithToken() {

        Raptor raptor = Utils.getRaptor();

        log.debug("subscribe with permission token");

        Raptor r = Utils.createNewInstance();
        r.Auth().login();

        Token t = r.Admin().Token().create(new Token("test", "test"));

        r.Admin().Token().Permission().set(t, PermissionUtil.asList(Permissions.admin));

        Device dev = r.Inventory().create(newDevice("dev"));
        Utils.waitFor(1500);

        Raptor r2 = new Raptor(Utils.loadSettings().getProperty("url"), t);

        r2.Inventory().subscribe(dev, new ActionCallback() {
            @Override
            public void callback(Action action, ActionPayload payload) {
                log.debug("dev: Data received  for {}: {}", payload.actionId, payload.data);
                Assert.assertTrue(action.name.equals("switch"));
                Assert.assertTrue(payload.data.equals("on"));
            }
        });

        Action action = dev.action("switch");
        raptor.Action().invoke(action, "on");

        Utils.waitFor(1000);
    }

    @Test
    public void checkFailingSubscribePermission() {

        Raptor raptor = Utils.getRaptor();

        log.debug("subscribe with failing permissions");

        Raptor r = Utils.createNewInstance();
        r.Auth().login();

        Token t = r.Admin().Token().create(new Token("test", "test"));
        r.Admin().Token().Permission().set(t, PermissionUtil.asList(Permissions.subscribe));

        Device dev = r.Inventory().create(newDevice("dev"));
        Utils.waitFor(1500);

        Raptor r2 = new Raptor(Utils.loadSettings().getProperty("url"), t);

        try {
            r2.Inventory().subscribe(dev, new DataCallback() {
                @Override
                public void callback(Stream stream, RecordSet record) {
                    log.debug("Got data: {}", record.toJson());
                }
            });
        } catch (Exception e) {
            log.debug("Exception: {}", e.getMessage());
        }

        Stream stream = dev.stream("test");

        RecordSet record = new RecordSet(stream);
        record.channel("string", "test1");

        raptor.Stream().push(record);

        Utils.waitFor(1000);
    }

    @Test
    public void checkSubscribeForStreamPermission() {

        Raptor raptor = Utils.getRaptor();

        log.debug("subscribe to stream topic with permissions (subscribe, pull)");

        Raptor r = Utils.createNewInstance();
        r.Auth().login();

        Token t = r.Admin().Token().create(new Token("test", "test"));
        r.Admin().Token().Permission().set(t, PermissionUtil.asList(Permissions.pull));

        Device dev = r.Inventory().create(newDevice("dev"));
        Utils.waitFor(1500);

        Raptor r2 = new Raptor(Utils.loadSettings().getProperty("url"), t);
        Stream stream = dev.stream("test");

        r2.Stream().subscribe(stream, new DataCallback() {
            @Override
            public void callback(Stream stream, RecordSet record) {
                log.debug("Got data: {}", record.toJson());
            }
        });

        RecordSet record = new RecordSet(stream);
        record.channel("string", "test1");
        raptor.Stream().push(record);

        Utils.waitFor(1000);
    }

    @Test
    public void checkSubscribeForActionPermission() {

        Raptor raptor = Utils.getRaptor();

        log.debug("subscribe to action topic with permissions (subscribe, execute)");

        Raptor r = Utils.createNewInstance();
        r.Auth().login();

        Token t = r.Admin().Token().create(new Token("test", "test"));
        r.Admin().Token().Permission().set(t, PermissionUtil.asList(Permissions.execute));

        Device dev = r.Inventory().create(newDevice("dev"));
        Utils.waitFor(1500);

        Raptor r2 = new Raptor(Utils.loadSettings().getProperty("url"), t);
        Action action = dev.getAction("switch");

        r2.Action().subscribe(action, new ActionCallback() {
            @Override
            public void callback(Action action, ActionPayload message) {
                log.debug("Got data: {}", message.data);
            }
        });

        raptor.Action().invoke(action, "on");

        Utils.waitFor(2000);

    }

}
