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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
        record.timestamp(new Date(Instant.now().toEpochMilli() - 1000 + 200));

        raptor.Stream().push(stream, record);
        record.timestamp(new Date(Instant.now().toEpochMilli() - 1000 + 400));

        raptor.Stream().push(stream, record);
        record.timestamp(new Date(Instant.now().toEpochMilli() - 1000 + 600));

        raptor.Stream().push(stream, record);
        record.timestamp(new Date(Instant.now().toEpochMilli() - 1000 + 800));
    }

    @Test
    public void watchDeviceEvents() {

        final AtomicBoolean done = new AtomicBoolean(false);

        Raptor raptor = Utils.getRaptor();

        log.debug("watch data events");

        final Device dev = Utils.createDevice(newDevice("dev"));

        raptor.Inventory().subscribe(dev, new DeviceCallback() {
            @Override
            public void callback(Device obj, DevicePayload message) {
                log.debug("Device event received {}", message.toString());
                Assert.assertEquals(obj.id(), dev.id());
                done.set(true);
            }
        });
        dev.addStream("test2", "foo", "boolean");
        dev.addAction("sleep");

        raptor.Inventory().update(dev);

        Utils.waitUntil(5, () -> !done.get());

    }

    @Test
    public void watchDeviceTreeEvents() {

        final AtomicBoolean done = new AtomicBoolean(false);

        Raptor raptor = Utils.getRaptor();

        log.debug("watch device tree events");

        final TreeNode node = raptor.Tree().create(TreeNode.create("parent"));

        raptor.Tree().subscribe(node, (TreeNode node1, TreeNodePayload message) -> {
            log.debug("TreeNode event received {}", message.toString());
            Assert.assertEquals(node1.getId(), node.getId());
            done.set(true);
        });

        final Device dev = raptor.Inventory().create(newDevice("dev"));
        raptor.Tree().add(node, dev);

        dev.addStream("test2", "foo", "boolean");
        dev.addAction("sleep");

        raptor.Inventory().update(dev);

        Utils.waitUntil(5, () -> !done.get());
    }

    @Test
    public void watchDeepTreeEvents() {

        Raptor raptor = Utils.getRaptor();

        log.debug("watch device tree events");

        final AtomicInteger eventsReceived = new AtomicInteger(4);

        final TreeNode root = raptor.Tree().create(TreeNode.create("root"));
        final TreeNode child1 = raptor.Tree().addChild(root, TreeNode.create("child1"));
        final TreeNode child2 = raptor.Tree().addChild(child1, TreeNode.create("child2"));
        final TreeNode child3 = raptor.Tree().addChild(child2, TreeNode.create("child3"));

        raptor.Tree().subscribe(root, (TreeNode node, TreeNodePayload message) -> {
            log.debug("TreeNode {} event received {}", node.getName(), message.toString());
            eventsReceived.decrementAndGet();
        });

        raptor.Tree().subscribe(child1, (TreeNode node, TreeNodePayload message) -> {
            log.debug("TreeNode {} event received {}", node.getName(), message.toString());
            eventsReceived.decrementAndGet();
        });

        raptor.Tree().subscribe(child2, (TreeNode node, TreeNodePayload message) -> {
            log.debug("TreeNode {} event received {}", node.getName(), message.toString());
            eventsReceived.decrementAndGet();
        });

        raptor.Tree().subscribe(child3, (TreeNode node, TreeNodePayload message) -> {
            log.debug("TreeNode {} event received {}", node.getName(), message.toString());
            eventsReceived.decrementAndGet();
        });

        final Device dev = raptor.Inventory().create(newDevice("dev1"));
        raptor.Tree().add(child3, dev);

        dev.description("updated description");

        raptor.Inventory().update(dev);

        Utils.waitUntil(5, () -> eventsReceived.get() == 0);

    }

    @Test
    public void watchDeviceDataEvents() {

        final AtomicInteger done = new AtomicInteger(2);

        Raptor raptor = Utils.createNewInstance();

        log.debug("watch data events");

        Device dev = Utils.createDevice(raptor, newDevice("dev"));

        raptor.Inventory().subscribe(dev, new DataCallback() {
            @Override
            public void callback(Stream stream, RecordSet record) {
                log.debug("dev: Data received {}", record.toJson());
                Assert.assertTrue(record.deviceId().equals(dev.getDevice().id()));
                Assert.assertTrue(stream.name().equals("test2"));
                done.decrementAndGet();
            }
        });

        pushData(dev);

        Utils.waitUntil(5, () -> done.get() > 0);
    }

    @Test
    public void watchDeviceActionEvents() {

        final AtomicBoolean done = new AtomicBoolean(false);

        Raptor raptor = Utils.createNewInstance();

        log.debug("watch action events");

        Device dev = Utils.createDevice(raptor, newDevice("dev"));

        raptor.Inventory().subscribe(dev, new ActionCallback() {
            @Override
            public void callback(Action action, ActionPayload payload) {
                log.debug("dev: Data received  for {}: {}", payload.actionId, payload.data);
                Assert.assertTrue(action.name().equals("switch"));
                Assert.assertTrue(payload.data.equals("on"));
                done.set(true);
            }
        });

        Action action = dev.action("switch");
        raptor.Action().invoke(action, "on");

        Utils.waitUntil(5, () -> !done.get());
    }

    @Test
    public void subscribeWithToken() {

        final AtomicBoolean done = new AtomicBoolean(false);

        Raptor raptor = Utils.getRaptor();

        log.debug("subscribe with permission token");

        Raptor r = Utils.createNewInstance();
        r.Auth().login();

        Token t = r.Admin().Token().create(new Token("test", "test"));

        r.Admin().Token().Permission().set(t, PermissionUtil.asList(Permissions.admin));

        Device dev = r.Inventory().create(newDevice("dev"));

        Raptor r2 = new Raptor(Utils.loadSettings().getProperty("url"), t);

        r2.Inventory().subscribe(dev, new ActionCallback() {
            @Override
            public void callback(Action action, ActionPayload payload) {
                log.debug("dev: Data received  for {}: {}", payload.actionId, payload.data);
                Assert.assertTrue(action.name().equals("switch"));
                Assert.assertTrue(payload.data.equals("on"));
                done.set(true);
            }
        });

        Action action = dev.action("switch");
        raptor.Action().invoke(action, "on");

        Utils.waitUntil(5, () -> !done.get());
    }

    @Test
    public void checkFailingSubscribePermission() {

        final AtomicBoolean done = new AtomicBoolean(false);

        log.debug("subscribe with failing permissions");

        Raptor r = Utils.createNewInstance();
        r.Auth().login();

        Token t = r.Admin().Token().create(new Token("test", "test"));
        r.Admin().Token().Permission().set(t, PermissionUtil.asList(Permissions.execute));

        List<String> perms = r.Admin().Token().Permission().get(t);
        Assert.assertEquals(1, perms.size());

        Device dev = r.Inventory().create(newDevice("dev"));
        Raptor r2 = new Raptor(Utils.loadSettings().getProperty("url"), t);

        try {
            r2.Inventory().subscribe(dev, new DataCallback() {
                @Override
                public void callback(Stream stream, RecordSet record) {
                    log.debug("Got data: {}", record.toJson());
                }
            });
        } catch (Exception e) {
            log.debug("Expected exception received: {}", e.getMessage());
            done.set(true);
        }

        Stream stream = dev.stream("test");

        RecordSet record = new RecordSet(stream);
        record.channel("string", "test1");

        r.Stream().push(record);

        Utils.waitUntil(5, () -> !done.get());
    }

    @Test
    public void checkSubscribeForStreamPermission() {

        final AtomicBoolean done = new AtomicBoolean(false);

        log.debug("subscribe to stream topic with permissions (subscribe, pull)");

        Raptor r = Utils.createNewInstance();
        r.Auth().login();

        Token t = r.Admin().Token().create(new Token("test", "test"));
        r.Admin().Token().Permission().set(t, PermissionUtil.asList(Permissions.pull));

        Device dev = r.Inventory().create(newDevice("dev"));

        Raptor r2 = new Raptor(Utils.loadSettings().getProperty("url"), t);
        Stream stream = dev.stream("test");

        r2.Stream().subscribe(stream, new DataCallback() {
            @Override
            public void callback(Stream stream, RecordSet record) {
                log.debug("Got data: {}", record.toJson());
                done.set(true);
            }
        });

        RecordSet record = new RecordSet(stream);
        record.channel("string", "test1");
        r.Stream().push(record);

        Utils.waitUntil(10, () -> !done.get());
    }

    @Test
    public void checkSubscribeForActionPermission() {

        final AtomicBoolean done = new AtomicBoolean(false);
        Raptor raptor = Utils.getRaptor();

        log.debug("subscribe to action topic with permissions (subscribe, execute)");

        Raptor r = Utils.createNewInstance();
        r.Auth().login();

        Token t = r.Admin().Token().create(new Token("test", "test"));
        r.Admin().Token().Permission().set(t, PermissionUtil.asList(Permissions.execute));

        Device dev = r.Inventory().create(newDevice("dev"));

        Raptor r2 = new Raptor(Utils.loadSettings().getProperty("url"), t);
        Action action = dev.action("switch");

        r2.Action().subscribe(action, new ActionCallback() {
            @Override
            public void callback(Action action, ActionPayload message) {
                log.debug("Got data: {}", message.data);
                done.set(true);
            }
        });

        raptor.Action().invoke(action, "on");

        Utils.waitUntil(5, () -> !done.get());
    }

}
