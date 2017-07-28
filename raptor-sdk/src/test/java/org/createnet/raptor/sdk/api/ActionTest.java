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

import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.Utils;
import org.createnet.raptor.models.data.ActionStatus;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.Action;
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
public class ActionTest {

    final Logger log = LoggerFactory.getLogger(ActionTest.class);

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
        d.name("data test " + System.currentTimeMillis());
        
        d.addAction("switch");
        d.addAction("dimming");
        d.addAction("battery");

        Assert.assertEquals(3, d.actions().size());

        log.debug("Creating {} device", d.name());

        device = d;
    }

    @After
    public void tearDown() {
    }

    @Test
    public void setStatus()  {

        log.debug("set action status");
        
        Device dev = Utils.createDevice(device);
        Action a = dev.action("switch");
        ActionStatus status = raptor.Action().setStatus(a, a.getStatus().status("on"));
        
        Assert.assertNotNull(status);
        Assert.assertEquals("on", status.status);
    }
    
    @Test
    public void getStatus()  {

        log.debug("get action status");
        
        Device dev = Utils.createDevice(device);
        Action a = dev.action("switch");
        ActionStatus status = raptor.Action().setStatus(a, a.getStatus().status("on"));
        
        ActionStatus s = raptor.Action().getStatus(a);
        
        Assert.assertNotNull(s);
        Assert.assertEquals(s.status, status.status);
        
    }
    
    @Test
    public void invoke()  {
        log.debug("invoke an action");
        Device dev = Utils.createDevice(device);
        Action a = dev.action("switch");
        raptor.Action().invoke(a, "foobar3000");
        //todo: add subscription check
    }

}
