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

import java.util.List;
import java.util.Properties;
import org.createnet.raptor.models.acl.PermissionUtil;
import org.createnet.raptor.models.acl.Permissions;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.exception.RequestException;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.Utils;
import org.createnet.raptor.sdk.config.Config;
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
public class TokenPermissionTest {

    Logger log = LoggerFactory.getLogger(TokenPermissionTest.class);

    static Raptor raptor;

    public TokenPermissionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        raptor = Utils.createNewInstance();        
    }

    @AfterClass
    public static void tearDownClass() {
        raptor.Auth.logout();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGet() {

        log.debug("Test get token permission");

        Token token = raptor.Admin.Token.create(new Token("test", "test"));

        assertNotNull(token);
        assertNotNull(token.getId());

        List<String> permissions = raptor.Admin.Token.Permission.get(token);

        log.debug("Got permissions {}", permissions);
        assertNotNull(permissions);
        assertEquals(0, permissions.size());
    }

    @Test
    public void testSet() {

        log.debug("Test set token permission");

        Token token = raptor.Admin.Token.create(new Token("test_set", "test"));

        assertNotNull(token);
        assertNotNull(token.getId());

        List<String> permissions = PermissionUtil.asList(Permissions.create, Permissions.pull, Permissions.push);
        List<String> result = raptor.Admin.Token.Permission.set(token, permissions);

        log.debug("Added permissions {}", permissions);
        log.debug("Response permissions {}", result);
        
        log.debug("Got permissions {}", permissions);
        assertNotNull(result);
        assertEquals(permissions.size(), result.size());
        
        testGet();
        
    }

    @Test
    public void testACLenforcement() {

        log.debug("Test ACL check on token permission");

        testSet();
        
        List<Token> tokens = raptor.Admin.Token.get();
        
        assertFalse(tokens.isEmpty());
        
        Token t = tokens.get(0);
        
        Properties p = Utils.loadSettings();
        Raptor r = new Raptor(new Config(p.getProperty("url"), t.getToken()));
        r.Auth.login();
        
        assertNotNull(r.Auth.getToken());
        assertNotNull(r.Auth.getUser());
        
        Device d = new Device();
        d.name = "test1";
        Stream s = d.addStream("test", "string");
        
        r.Device.create(d);
        
        RecordSet record = new RecordSet(s);
        record.addRecord(RecordSet.createRecord(s, "test", "hello world"));
        r.Stream.push(record);
        
        try {
            r.Device.delete(d);
        }
        catch(RequestException ex) {
            assertEquals(403, ex.getStatus());
        }
        
    }

}
