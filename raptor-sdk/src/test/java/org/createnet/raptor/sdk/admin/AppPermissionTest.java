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
package org.createnet.raptor.sdk.admin;

import java.util.Arrays;
import org.createnet.raptor.models.acl.EntityType;
import org.createnet.raptor.models.app.App;
import org.createnet.raptor.models.auth.DefaultGroups;
import org.createnet.raptor.models.auth.StaticGroup;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.Utils;
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
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class AppPermissionTest {

    Logger log = LoggerFactory.getLogger(AppPermissionTest.class);

    public AppPermissionTest() {
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

    protected App createApp(Raptor r) {

        assertTrue(r.Auth().getUser().isAdmin());

        App app = new App(Utils.rndName(EntityType.app));

        Raptor r1 = Utils.createNewUserInstance();
        app.addUser(r1.Auth().getUser(), DefaultGroups.user.getName());

        Device dev = Utils.createDevice(r, new Device().name(Utils.rndName(EntityType.device)));

        app.addDevice(dev);
        
        r.App().create(app);
        
        assertEquals(r.Auth().getUser().getId(), app.getUserId());
        assertEquals(2, app.getGroups().size());
        assertEquals(1, app.getDevices().size());
        assertEquals(r.Auth().getUser().getId(), app.getOwnerId());
        
        log.debug("Created app {}", app.getName());
        
        return app;
    }

    @Test
    public void testCreateFullApp() {
        Raptor r = Utils.createNewAdminInstance();
        App app = createApp(r);
    }
    
    @Test
    public void testUserAppPermission() {
        
        Raptor r = Utils.createNewAdminInstance();
        App app = createApp(r);
        
        Raptor r1 = Utils.createNewUserInstance();
        
        app.addUser(r1.Auth().getUser(), Arrays.asList(StaticGroup.user.name()));
        r.App().update(app);
        
        //2 user +1 admin (owner)
        assertEquals(3, app.getUsers().size());
        
        Device dev = r1.Inventory().load(app.getDevices().get(0));
        
        log.debug("Created device {} by app user {}", app.getName(), r1.Auth().getUser().getUsername());
        
    }

}
