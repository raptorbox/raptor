/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.UUID;
import org.createnet.raptor.models.objects.RaptorComponent;
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

//    assertTrue(device.parentId == null);
//    assertTrue(device.path == null);
        assertTrue(device.streams().size() == 1);
        assertTrue(device.streams().get("mylocation").channels.get("position").type.toLowerCase().equals("geo_point"));

        assertTrue(device.actions().size() == 3);
        assertTrue(device.actions().get("makeCall") != null);

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
        
        assertTrue(d.stream("test").channels.size() == 3);
        
        assertNotNull(d.stream("test").channels.get("boolean"));
        assertNotNull(d.stream("test").channels.get("number"));
        assertNotNull(d.stream("test").channels.get("string"));        
        
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
