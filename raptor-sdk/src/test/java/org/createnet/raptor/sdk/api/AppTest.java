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

import java.util.List;
import org.createnet.raptor.models.app.App;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.Utils;
import org.createnet.raptor.sdk.PageResponse;
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
public class AppTest {

    final Logger log = LoggerFactory.getLogger(AppTest.class);

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
    public void list() {

        Raptor raptor = Utils.createNewAdminInstance();

        log.debug("List apps");

        PageResponse<App> pager = raptor.App().list();
        List<App> list = pager.getContent();

        log.debug("found {} apps", list.size());
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    public void create() {

        Raptor raptor = Utils.createNewAdminInstance();

        log.debug("create app");

        App app = new App();
        app.setName("test_" + System.currentTimeMillis());
        app.setUserId(raptor.Auth().getUser().getId());

        raptor.App().create(app);

        PageResponse<App> pager = raptor.App().list();
        List<App> list = pager.getContent();

        log.debug("found {} apps", list.size());
        assertNotNull(list);
        assertFalse(list.isEmpty());

    }

    @Test
    public void delete() {

        Raptor raptor = Utils.createNewAdminInstance();

        log.debug("create app");

        App app = new App();
        app.setName("test_" + System.currentTimeMillis());
        app.setUserId(raptor.Auth().getUser().getId());

        PageResponse<App> pager = raptor.App().list();
        List<App> list = pager.getContent();
        int len = list.size();
        log.debug("found {} apps", len);
        
        raptor.App().create(app);
        raptor.App().delete(app);

        pager = raptor.App().list();
        list = pager.getContent();

        log.debug("found {} apps", list.size());
        assertNotNull(list);
        assertEquals(len, list.size());

    }

}
