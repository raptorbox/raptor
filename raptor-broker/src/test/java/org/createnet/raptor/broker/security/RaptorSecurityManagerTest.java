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
package org.createnet.raptor.broker.security;

import java.util.HashSet;
import java.util.Set;
import org.apache.activemq.artemis.core.security.CheckType;
import org.apache.activemq.artemis.core.security.Role;
import org.createnet.raptor.broker.configuration.BrokerConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class RaptorSecurityManagerTest {

    final private RaptorSecurityManager manager = new RaptorSecurityManager();

    String roleUser = "user";
    String roleAdmin = "admin";

    public RaptorSecurityManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {


        BrokerConfiguration brokerConfiguration = new BrokerConfiguration();

        brokerConfiguration.artemisConfiguration = "file:///etc/raptor/broker.xml";

        BrokerConfiguration.BrokerUser user = new BrokerConfiguration.BrokerUser(roleUser, roleUser);
        user.addRole(roleUser);

        brokerConfiguration.users.add(user);

        user = new BrokerConfiguration.BrokerUser(roleAdmin, roleAdmin);
        user.addRole(roleAdmin);
        brokerConfiguration.users.add(user);

        manager.setBrokerConfiguration(brokerConfiguration);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLocalUser() {
        assertTrue(manager.validateUser(roleUser, roleUser));
        assertTrue(manager.validateUser(roleAdmin, roleAdmin));
//    assertFalse(manager.validateUser("thief", "dude"));
    }

    @Test
    public void testLocalUserRoles() {

        Set<Role> roles = new HashSet();

        roles.add(new Role(roleAdmin, true, true, true, true, true, true, true));

        String address = "myobject.something";

        assertFalse(
                manager.validateUserAndRole(
                        roleUser, roleUser,
                        roles,
                        CheckType.CONSUME,
                        address,
                        null
                )
        );

        address = "anything";

        assertTrue(
                manager.validateUserAndRole(
                        roleAdmin, roleAdmin,
                        roles,
                        CheckType.CREATE_NON_DURABLE_QUEUE,
                        address,
                        null
                )
        );
    }

}
