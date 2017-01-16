/*
 * Copyright 2017 Luca Capra <luca.capra@fbk.eu>.
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
package org.createnet.raptor.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.createnet.raptor.client.model.AuthClient;
import org.createnet.raptor.models.objects.ServiceObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public class RaptorTest {
//    
//    public RaptorTest() {
//    }
//    
//    @BeforeClass
//    public static void setUpClass() {
//    }
//    
//    @AfterClass
//    public static void tearDownClass() {
//    }
//    
//    @Before
//    public void setUp() {
//    }
//    
//    @After
//    public void tearDown() {
//    }

    @Test
    public void testSomeMethod() {

        RaptorClient.ClientConfig config = new RaptorClient.ClientConfig();

//        config.url = "http://localhost";
        config.username = "admin";
        config.password = "admin";

        Raptor raptor = new Raptor(config);
        AuthClient.LoginResponseBody result = raptor.auth().login();

        assertNotNull(result);
        assertNotNull(result.token);
        

    }

    @Test
    public void testCreate() throws InterruptedException {

        RaptorClient.ClientConfig config = new RaptorClient.ClientConfig();

//        config.url = "http://localhost";
        config.username = "admin";
        config.password = "admin";

        Raptor raptor = new Raptor(config);
        AuthClient.LoginResponseBody result = raptor.auth().login();

        assertNotNull(result);
        assertNotNull(result.user);
        assertNotNull(result.token);
        
        ServiceObject obj = new ServiceObject();

        obj.name = "test1";
        obj.addStream("enviromental")
                .addChannel("temperature", "number")
                .addChannel("pressure", "number");
        obj.addAction("reset");

        raptor.serviceObject().create(obj);

        assertNotNull(obj.id);

    }

}
