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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.createnet.raptor.models.acl.EntityType;
import org.createnet.raptor.models.acl.Operation;
import org.createnet.raptor.models.app.App;
import org.createnet.raptor.models.auth.AclApp;
import org.createnet.raptor.models.auth.Group;
import org.createnet.raptor.models.auth.Permission;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.auth.request.AuthorizationResponse;
import org.createnet.raptor.models.exception.RequestException;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.sdk.Utils;
import org.createnet.raptor.sdk.api.AuthClient;
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
public class GroupTest {

    final Logger log = LoggerFactory.getLogger(GroupTest.class);

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
    public void createAdminUser() {
        Raptor adm1 = Utils.createNewAdminInstance();
        assertTrue(adm1.Auth().getUser().isAdmin());
    }

    @Test
    public void createStandardUser() {
        Raptor adm1 = Utils.createNewUserInstance();
        assertFalse(adm1.Auth().getUser().isAdmin());
    }

    @Test
    public void listGroups() {

        Raptor adm1 = Utils.createNewAdminInstance();
        List<Group> list = adm1.Admin().Group().list();

        assertNotNull(list);
        assertTrue(!list.isEmpty());
        assertTrue(list.size() >= 2);
    }

    @Test
    public void createGroup() {

        Raptor adm1 = Utils.createNewAdminInstance();
        String groupName = "g" + adm1.Auth().getUser().getUsername();
        Group group = new Group(groupName, Arrays.asList(
                new Permission(EntityType.app, Operation.admin),
                new Permission(EntityType.device, Operation.read),
                new Permission(EntityType.action, Operation.execute)
        ));
        adm1.Admin().Group().create(group);

        List<Group> list = adm1.Admin().Group().list();
        assertNotNull(list);
        assertTrue(list.size() >= 3);

        Optional<Group> optGroup = list.stream().filter((g) -> g.getName().equals(groupName)).findFirst();

        assertTrue(optGroup.isPresent());
        assertTrue(optGroup.get().getPermissions().contains(new Permission("device_read")));

    }

    @Test
    public void updateGroup() {

        Raptor adm1 = Utils.createNewAdminInstance();
        String groupName = "g" + adm1.Auth().getUser().getUsername();

        Permission app_admin = new Permission(EntityType.app, Operation.admin);
        Permission app_read = new Permission(EntityType.app, Operation.read);
        Permission action_exec = new Permission(EntityType.action, Operation.execute);

        Group group = new Group(groupName, Arrays.asList(app_admin));
        adm1.Admin().Group().create(group);

        Group g1 = adm1.Admin().Group().read(group.getId());
        assertTrue(g1.getPermissions().size() == 1);
        assertTrue(g1.getPermissions().contains(app_admin));

        g1.getPermissions().remove(app_admin);
        g1.getPermissions().add(app_read);
        g1.getPermissions().add(action_exec);

        adm1.Admin().Group().update(g1);

        Group g2 = adm1.Admin().Group().read(g1.getId());

        assertTrue(g2.getPermissions().size() == 2);
        assertTrue(g2.getPermissions().contains(action_exec));
        assertTrue(g2.getPermissions().contains(app_read));
        assertFalse(g2.getPermissions().contains(app_admin));

    }

    @Test
    public void deleteGroup() {

        Raptor adm1 = Utils.createNewAdminInstance();
        String groupName = "g" + adm1.Auth().getUser().getUsername();

        Permission app_read = new Permission(EntityType.app, Operation.read);

        Group group = new Group(groupName, Arrays.asList(app_read));
        adm1.Admin().Group().create(group);

        Group g1 = adm1.Admin().Group().read(group.getId());
        assertTrue(g1.getPermissions().size() == 1);
        assertTrue(g1.getPermissions().contains(app_read));

        adm1.Admin().Group().delete(g1);
        
        try {
            Group g3 = adm1.Admin().Group().read(g1.getId());
            fail("Group has not been deleted");
        }
        catch(RequestException ex) {
            assertEquals(ex.getStatus(), 404);
        }

    }

}
