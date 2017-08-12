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

import java.time.Instant;
import java.util.Properties;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.Utils;
import org.createnet.raptor.sdk.exception.AuthenticationFailedException;
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
public class AuthTest {

    final Logger log = LoggerFactory.getLogger(AuthTest.class);

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

    @Test
    public void login() {
        log.debug("Try to login");
        Properties p = Utils.loadSettings();
        AuthClient.LoginState loginInfo = raptor.Auth().login(p.getProperty("username"), p.getProperty("password"));
        Assert.assertEquals(loginInfo.user.getUsername(), p.getProperty("username"));
        Assert.assertNotNull(loginInfo.token);
    }

    @Test(expected = AuthenticationFailedException.class)
    public void failLogin() {
        log.debug("Try to fake login");
        AuthClient.LoginState loginInfo = raptor.Auth().login("admin", "apple");
    }

    @Test(expected = AuthenticationFailedException.class)
    public void failTokenLogin() {

        log.debug("Try to fake token login");

        Properties p = Utils.loadSettings();
        Raptor r = new Raptor(p.getProperty("url"), "my fancy token");

        AuthClient.LoginState res = r.Auth().login();
    }

    @Test
    public void multipleLogin() {

        // ensure we have a token
        Utils.getRaptor().Auth().login();

        Raptor r1 = Utils.createNewInstance();
        log.debug("test1 {}", r1.Auth().getUser().getUuid());

        Raptor r2 = Utils.createNewInstance();
        log.debug("test2 {}", r2.Auth().getUser().getUuid());

        Assert.assertNotEquals(r1.Auth().getUser().getUuid(), r2.Auth().getUser().getUuid());

        log.debug("Try to login again test1");
        AuthClient.LoginState s1 = r1.Auth().login();
        log.debug("Try to login again test2");
        AuthClient.LoginState s2 = r2.Auth().login();

        log.debug("{} vs {}", r1.Auth().getUser().getUuid(), r2.Auth().getUser().getUuid());

        Assert.assertNotEquals(r1.Auth().getUser().getUuid(), r2.Auth().getUser().getUuid());

        Assert.assertNotEquals(s1.token, s2.token);
        Assert.assertNotEquals(s1.user.getUuid(), s2.user.getUuid());

    }

    @Test
    public void refresh() {

        log.debug("Refresh token");

        Properties p = Utils.loadSettings();
        long now = Instant.now().toEpochMilli();
        
        AuthClient.LoginState loginInfo = raptor.Auth().login();
        AuthClient.LoginState refreshInfo = raptor.Auth().refreshToken();

        Assert.assertNotNull(refreshInfo.token);
        Assert.assertNotEquals(refreshInfo.token, loginInfo.token);
        Assert.assertTrue(now < refreshInfo.expires);
        
        log.debug("Refresh token, again");
        AuthClient.LoginState refreshInfo2 = raptor.Auth().refreshToken();

        Assert.assertNotNull(refreshInfo2.token);
        Assert.assertNotEquals(refreshInfo.token, refreshInfo2.token);
        Assert.assertTrue(now < refreshInfo2.expires);
        
    }

    @Test
    public void logout() {

        log.debug("Logout");

        Properties p = Utils.loadSettings();

        AuthClient.LoginState loginInfo = raptor.Auth().login();

        raptor.Auth().logout();

        Assert.assertNull(raptor.Auth().getUser());
        Assert.assertNull(raptor.Auth().getToken());
    }

}
