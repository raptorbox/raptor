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
package org.createnet.raptor.objects;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.utils.TestUtils;
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
public class DeviceTest extends TestUtils {

    protected JsonNode jsonData;

    public DeviceTest() {

    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws IOException {
        loadObject();
        jsonData = loadData("record");
    }

    @After
    public void tearDown() {

    }

    /**
     * Test of parse method, of class Device.
     *
     * @throws
     * org.createnet.raptor.models.objects.RaptorComponent.ParserException
     */
    @Test
    public void testParse() {

        device.parse(jsonDevice.toString());

        assertTrue(device.name().equals("Phone"));
        assertTrue(device.streams().size() == 1);
        assertTrue(device.streams().get("mylocation").channels().get("position").type().toLowerCase().equals("geo_point"));

        assertTrue(device.actions().size() == 3);
        assertTrue(device.actions().get("makeCall") != null);

    }

    /**
     * Test json parse
     *
     * @throws
     * org.createnet.raptor.models.objects.RaptorComponent.ParserException
     */
    @Test
    public void testParse2() {

        jsonDevice = loadData("device2");
        Device dev = Device.fromJSON(jsonDevice);

        assertEquals(dev.name(), jsonDevice.get("name").asText());

    }

    /**
     * Test of parse method, of class Device.
     *
     * @throws
     * org.createnet.raptor.models.objects.RaptorComponent.ParserException
     */
    @Test
    public void testAddStreams() {
        
        Device d = new Device();
        
        d.addStream("test", "boolean", "boolean");
        d.addStream("test", "number", "number");
        d.addStream("test", "string", "string");
        
        assertTrue(d.stream("test").channels().size() == 3);
        
        assertNotNull(d.stream("test").channels().get("boolean"));
        assertNotNull(d.stream("test").channels().get("number"));
        assertNotNull(d.stream("test").channels().get("string"));        
        
    }

    @Test
    public void testSerializeToJsonNode() {

        device.parse(jsonDevice.toString());

        JsonNode node = device.toJsonNode();

        assertTrue(node.has("name"));

    }

    @Test
    public void testSerializeViewPublic() throws IOException {

        device.parse(jsonDevice.toString());

        String strjson = device.toJSON();
        JsonNode json = mapper.readTree(strjson);

        assertTrue(json.has("userId"));
    }

    /**
     * Test of isNew method, of class Device.
     */
    @Test
    public void testIsNew() {
        assertTrue((new Device()).isNew());
    }

}
