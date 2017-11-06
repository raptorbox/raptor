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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.createnet.raptor.models.acl.EntityType;
import org.createnet.raptor.models.acl.Operation;
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
public class UserTest {

    final Logger log = LoggerFactory.getLogger(UserTest.class);

    public static Raptor raptor;

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

    @After
    public void tearDown() {
    }

    private String rndUsername() {
        return Utils.rndName("user");
    }

    @Test
    public void receiveUserUpdate() {

        AtomicBoolean done = new AtomicBoolean(false);

        String username = rndUsername();
        log.debug("Create user {}", username);
        Raptor r1 = Utils.createNewUserInstance(username);
        final User user = r1.Auth().getUser();

        raptor.Admin().User().subscribe(user, (u, message) -> {

            assertEquals(false, message.getUser().getEnabled());
            assertEquals(user.getUuid(), u.getUuid());

            done.set(true);
        });

        user.setEnabled(false);
        raptor.Admin().User().update(user);

        Utils.waitUntil(5, () -> !done.get());
    }

    @Test
    public void createUser() {

        String username = rndUsername();
        log.debug("Create user {}", username);
        Raptor r1 = Utils.createNewUserInstance(username);
        final User user = r1.Auth().getUser();

        assertEquals(username, user.getUsername());
        assertNotNull(user.getUuid());
    }

    @Test
    public void updateUser() {

        
        String username = rndUsername();
        log.debug("Create user {}", username);
        
        Raptor r1 = Utils.createNewUserInstance(username);

        final User user = r1.Auth().getUser();
        String email = user.getEmail();
        
        user.setEmail(username + "_new_cool_email@example.com");
        user.setEnabled(false);

        User updatedUser = raptor.Admin().User().update(user);

        assertNotEquals(email, updatedUser.getEmail());
        assertEquals(false, updatedUser.getEnabled());

    }

    @Test
    public void failDuplicateEmail() {


        String username1 = rndUsername();
        log.debug("Create user1 {}", username1);
        Raptor r1 = Utils.createNewUserInstance(username1);
        User user1 = r1.Auth().getUser();

        String username2 = rndUsername();
        log.debug("Create user2 {}", username2);
        Raptor r2 = Utils.createNewUserInstance(username2);
        User user2 = r1.Auth().getUser();

        user1.setUsername(username2);
        user1.setEmail(user2.getEmail());

        try {
            User updatedUser1 = raptor.Admin().User().update(user1);
        } catch (RequestException e) {
            assertEquals(e.getStatus(), 400);
            return;
        }

        fail("Should not update user1");

    }

    @Test
    public void failDuplicatedUsername() {

        String username1 = rndUsername();
        log.debug("Create user1 {}", username1);
        Raptor r1 = Utils.createNewUserInstance(username1);
        User user1 = r1.Auth().getUser();

        String username2 = rndUsername();
        log.debug("Create user2 {}", username2);
        Raptor r2 = Utils.createNewUserInstance(username2);
        User user2 = r1.Auth().getUser();

        user1.setUsername(username2);

        try {
            User updatedUser1 = raptor.Admin().User().update(user1);
        } catch (RequestException ex) {
            assertEquals(400, ex.status);
            return;
        }

        throw new RuntimeException("Duplicated username allowed?");
    }

    @Test
    public void deleteUser() {

        String username = rndUsername();
        log.debug("Create user1 {}", username);
        Raptor r1 = Utils.createNewUserInstance(username);
        final User user1 = r1.Auth().getUser();

        raptor.Admin().User().delete(user1);

        try {
            raptor.Admin().User().get(user1.getUuid());
            throw new RuntimeException("User should not exists");
        } catch (RequestException ex) {
            assertEquals(404, ex.status);
        }

    }

    @Test
    public void impersonateUser() {

        String username = rndUsername();
        log.debug("Create user1 {}", username);
        Raptor r1 = Utils.createNewUserInstance(username);
        User user1 = r1.Auth().getUser();

        AuthClient.LoginState state = raptor.Admin().User().impersonate(user1.getUuid());

        Raptor r2 = new Raptor(raptor.getConfig().getUrl(), state.token);
        User user2 = r2.Admin().User().get();

        assertNotNull(user2);
        assertEquals(user2.getUuid(), user1.getUuid());
        assertEquals(user2.getUsername(), user1.getUsername());

    }

    @Test
    public void isAuthorized() throws IOException {

        Raptor r1 = Utils.createNewUserInstance();

        Raptor r2 = Utils.createNewUserInstance();
        String userId2 = r2.Auth().getUser().getUuid();

        Device dev1 = new Device();
        dev1.name("auth_test");
        r1.Inventory().create(dev1);

        assertNotNull(dev1.id());

        AuthorizationResponse res;

        res = r1.Admin().User().isAuthorized(dev1, Operation.admin);
        assertEquals(res.result, true);

        res = r1.Admin().User().isAuthorized(dev1, Operation.read);
        assertEquals(res.result, true);

        res = r1.Admin().User().isAuthorized(userId2, EntityType.device, Operation.admin, dev1.id());
        assertEquals(res.result, false);

        res = r1.Admin().User().isAuthorized(userId2, EntityType.device, Operation.read, dev1.id());
        assertEquals(res.result, false);

    }

}
