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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.exception.RequestException;
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
public class PreferenceTest {
    
    final Logger log = LoggerFactory.getLogger(PreferenceTest.class);
    
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
    public void setPreference()  {
        
        Raptor r = Utils.createNewInstance();
        
        ObjectNode json = r.Admin.User.Preferences.newObjectNode();
        
        json.put("test", "foo");
        json.put("size", 1000L);
        json.put("valid", true);
        
        JsonNode response = r.Admin.User.Preferences.set("test1", json);
        
        assertEquals(response.get("size").asLong(), json.get("size").asLong());
        
        
    }
    
}
