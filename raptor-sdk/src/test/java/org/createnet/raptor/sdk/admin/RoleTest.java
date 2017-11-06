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
import java.util.List;
import java.util.Optional;
import org.createnet.raptor.models.acl.EntityType;
import org.createnet.raptor.models.acl.Operation;
import org.createnet.raptor.models.auth.Role;
import org.createnet.raptor.models.auth.Permission;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.models.exception.RequestException;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.sdk.PageResponse;
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
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public class RoleTest {

    final Logger log = LoggerFactory.getLogger(RoleTest.class);

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
    public void listRoles() {

        Raptor adm1 = Utils.createNewAdminInstance();
        PageResponse<Role> list = adm1.Admin().Role().list();

        assertNotNull(list);
        assertTrue(!list.getContent().isEmpty());
        assertTrue(list.getContent().size() >= 2);
    }

    @Test
    public void createRole() {

        Raptor adm1 = Utils.createNewAdminInstance();
        String groupName = "g" + adm1.Auth().getUser().getUsername();
        Permission p1 = new Permission(EntityType.device, Operation.read);
        Role group = new Role(groupName, Arrays.asList(
                new Permission(EntityType.app, Operation.admin),
                p1,
                new Permission(EntityType.action, Operation.execute)
        ));
        adm1.Admin().Role().create(group);

        PageResponse<Role> list = adm1.Admin().Role().list();
        assertNotNull(list);
        assertTrue(list.getContent().size() >= 3);

        Optional<Role> optRole = list.getContent().stream().filter((g) -> g.getName().equals(groupName)).findFirst();

        assertTrue(optRole.isPresent());
        assertTrue(optRole.get().getPermissions().contains(p1));

    }

    @Test
    public void updateRole() {

        Raptor adm1 = Utils.createNewAdminInstance();
        String groupName = "g" + adm1.Auth().getUser().getUsername();

        Permission app_admin = new Permission(EntityType.app, Operation.admin);
        Permission app_read = new Permission(EntityType.app, Operation.read);
        Permission action_exec = new Permission(EntityType.action, Operation.execute);

        Role group = new Role(groupName, Arrays.asList(app_admin));
        adm1.Admin().Role().create(group);

        Role g1 = adm1.Admin().Role().read(group.getId());
        assertTrue(g1.getPermissions().size() == 1);
        assertTrue(g1.getPermissions().contains(app_admin));

        g1.getPermissions().remove(app_admin);
        g1.getPermissions().add(app_read);
        g1.getPermissions().add(action_exec);

        adm1.Admin().Role().update(g1);

        Role g2 = adm1.Admin().Role().read(g1.getId());

        assertTrue(g2.getPermissions().size() == 2);
        assertTrue(g2.getPermissions().contains(action_exec));
        assertTrue(g2.getPermissions().contains(app_read));
        assertFalse(g2.getPermissions().contains(app_admin));

    }

    @Test
    public void deleteRole() {

        Raptor adm1 = Utils.createNewAdminInstance();
        String groupName = "g" + adm1.Auth().getUser().getUsername();

        Permission app_read = new Permission(EntityType.app, Operation.read);

        Role group = new Role(groupName, Arrays.asList(app_read));
        adm1.Admin().Role().create(group);

        Role g1 = adm1.Admin().Role().read(group.getId());
        assertTrue(g1.getPermissions().size() == 1);
        assertTrue(g1.getPermissions().contains(app_read));

        adm1.Admin().Role().delete(g1);
        
        try {
            Role g3 = adm1.Admin().Role().read(g1.getId());
            fail("Role has not been deleted");
        }
        catch(RequestException ex) {
            assertEquals(ex.getStatus(), 404);
        }

    }

    @Test
    public void testRoleEnforcement() {

        Raptor adm1 = Utils.createNewAdminInstance();
        Raptor usr1 = Utils.createNewUserInstance();
        
        User admin = adm1.Auth().getUser();
        User user = usr1.Auth().getUser();
        
        // read all users
        Permission user_read = new Permission(EntityType.user, Operation.read);
        //admin own devices
        Permission device_admin_own = new Permission(EntityType.device, Operation.admin, true);
        
        String groupName = "g" + adm1.Auth().getUser().getUsername();
        Role group = new Role(groupName, Arrays.asList(user_read, device_admin_own));
        adm1.Admin().Role().create(group);
       
        user.addRole(group);
        adm1.Admin().User().update(user);
        
        assertTrue(user.hasPermission(user_read));
        assertTrue(user.hasPermission(device_admin_own));

        // read own profile
        User u1 = usr1.Admin().User().get(user.getUuid());
        assertEquals(user.getUuid(), u1.getUuid());
        
        // should be allowed to read other user info
        User a1 = usr1.Admin().User().get(admin.getUuid());
        assertEquals(admin.getUuid(), a1.getUuid());
        
        Device dev = usr1.Inventory().create(new Device().name("test"));
        dev.name("test updated");
        usr1.Inventory().update(dev);
        usr1.Inventory().delete(dev);
        
    }    
    
}
