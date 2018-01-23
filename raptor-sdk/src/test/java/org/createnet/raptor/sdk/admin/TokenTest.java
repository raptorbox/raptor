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
import org.createnet.raptor.models.auth.AclDevice;
import org.createnet.raptor.models.auth.Token;
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
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public class TokenTest {

    final Logger log = LoggerFactory.getLogger(TokenTest.class);

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
    public void listToken() {

        Raptor raptor = Utils.createNewAdminInstance();

        List<Token> tokens = raptor.Admin().Token().list().getContent();

        assertNotNull(tokens);
        assertTrue(tokens.size() > 0);
        
        log.debug("Create new token for user {}", raptor.Auth().getUser().getId());

        Token newToken = raptor.Admin().Token().create(new Token("test", "secret" + System.currentTimeMillis() * Math.random()));

        tokens = raptor.Admin().Token().list().getContent();

        assertNotNull(tokens);
        assertTrue(tokens.size() > 0);
        
        Token savedToken = raptor.Admin().Token().read(newToken.getId());
        assertEquals(newToken.getToken(), savedToken.getToken());

    }

    @Test
    public void createToken() {

        Raptor raptor = Utils.createNewAdminInstance();

        Token token = new Token("test", "secret" + System.currentTimeMillis() * Math.random());
        //fake token
        String faketoken = "foobar";
        token.setToken(faketoken);

        Token newToken = raptor.Admin().Token().create(token);

        assertNotNull(newToken);
        assertNotNull(newToken.getId());
        assertNotEquals(faketoken, newToken.getToken());

        assertTrue(newToken.isEnabled());
        assertFalse(newToken.isExpired());

    }

    @Test
    public void loadToken() {

        Raptor raptor = Utils.createNewAdminInstance();

        Token token = new Token("test", "secret" + System.currentTimeMillis() * Math.random());
        Token newToken = raptor.Admin().Token().create(token);

        Token savedToken = raptor.Admin().Token().read(newToken.getId());

        assertEquals(newToken.getToken(), savedToken.getToken());
    }

    @Test
    public void currentToken() {

        Raptor raptor = Utils.createNewAdminInstance();
        
        Token loginToken = raptor.Admin().Token().current();
        
        assertEquals(loginToken.getToken(), raptor.Auth().getToken());
        assertEquals(loginToken.getType(), Token.Type.LOGIN);
        
        Token token = new Token("test", "secret" + System.currentTimeMillis() * Math.random());
        Token newToken = raptor.Admin().Token().create(token);

        Token savedToken = raptor.Admin().Token().read(newToken.getId());

        assertEquals(savedToken.getType(), Token.Type.DEFAULT);
        
    }

    @Test
    public void updateTokenChangingSecret() {

        Raptor raptor = Utils.createNewAdminInstance();

        String secret = "secret_" + System.currentTimeMillis() * Math.random();
        String name = "token_" + System.currentTimeMillis() * Math.random();

        Token newToken = raptor.Admin().Token().create(new Token(name, secret));
        String token = newToken.getToken();

        assertNotNull(newToken);

        newToken.setDevice(new AclDevice("foobar"));
        newToken.setSecret("test2");
        newToken.setUser(raptor.Auth().getUser());
        newToken.setEnabled(false);
        newToken.setExpires(1L);

        Token updatedToken = raptor.Admin().Token().update(newToken);

        assertNotNull(updatedToken);
        assertNotNull(newToken.getId());

        assertNotEquals(secret, updatedToken.getSecret());
        assertNotEquals(token, updatedToken.getToken());

        assertFalse(updatedToken.isEnabled());

        Utils.waitFor(1000); // wait for expiration
        log.debug("Expired {}: {}", updatedToken.isExpired(), updatedToken.getExpiresInstant().toString());
        assertTrue(updatedToken.isExpired());

    }

    @Test
    public void updateWithSameSecret() {

        Raptor raptor = Utils.createNewAdminInstance();

        String secret = "foobar1";
        String name = Utils.rndName("token");

        Token newToken = raptor.Admin().Token().create(new Token(name, secret));
        String token = newToken.getToken();

        assertNotNull(newToken);

        newToken.setDevice(new AclDevice("foobar"));
        newToken.setUser(raptor.Auth().getUser());
        newToken.setEnabled(false);
        newToken.setExpires(1L);

        Token updatedToken = raptor.Admin().Token().update(newToken);

        assertNotNull(updatedToken);
        assertNotNull(newToken.getId());

        assertEquals(secret, updatedToken.getSecret());
        assertEquals(token, updatedToken.getToken());

        assertFalse(updatedToken.isEnabled());

        Utils.waitFor(1000); // wait for expiration
        assertTrue(updatedToken.isExpired());
    }

}
