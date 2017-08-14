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
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.Stream;
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
public class RecordsetTest extends TestUtils {

    public RecordsetTest() {

    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        loadObject();
    }

    @After
    public void tearDown() {

    }

    /**
     * Test of parse method, of class Device.
     *
     * @throws org.createnet.raptor.models.exception.RecordsetException
     * @throws com.fasterxml.jackson.core.JsonProcessingException
     */
    @Test
    public void testParseRecord() throws IOException {

        JsonNode data = loadData("record");

        Stream stream = device.streams().get("mylocation");
        RecordSet records = mapper.readValue(data.toString(), RecordSet.class);

        records.userId("Mr. foobar");

        Boolean channel = records.value("happy").getBoolean();
        assertTrue(channel.equals(true));

        String strjson = records.toJson();
        JsonNode json = mapper.readTree(strjson);

        assertTrue(json.get("channels").get("happy").isBoolean());
        assertTrue(!json.get("userId").isNull());

    }

    @Test
    public void testParseRecordNewTypes() throws IOException {

        JsonNode devjson = loadData("device2");
        Device dev = Device.fromJSON(devjson);
        
        JsonNode data = loadData("record2");

        Stream stream = device.streams().get("data");
        RecordSet record = mapper.readValue(data.toString(), RecordSet.class);
        
        assertTrue(record.channel("foobar").getObject() != null);
        assertTrue(record.channel("list").getList().size() == 5);
        assertTrue(record.channel("position").getPosition().getX() == 11.45);

    }

    @Test
    public void testParseStreamData() throws IOException {

        JsonNode data1 = loadData("record1");
        RecordSet records = mapper.readValue(data1.toString(), RecordSet.class);

        String strjson = records.toJson();
        JsonNode json = mapper.readTree(strjson);

        assertTrue(json.has("channels"));
        assertTrue(json.has("timestamp"));

    }

    @Test
    public void testParseResultSet() throws IOException {

        JsonNode resultset = loadData("resultset");
        ResultSet results = mapper.readValue(resultset.toString(), ResultSet.class);

        String strjson = results.toJson();
        JsonNode json = mapper.readTree(strjson);

        assertTrue(json.size() == results.size());

    }

    @Test(expected = RaptorComponent.ValidationException.class)
    public void testParseRecordSet() throws IOException {

        JsonNode resultset = loadData("resultset");
        JsonNode json = resultset.get(0);

        device.parse(jsonDevice);

        Stream stream = device.streams().getOrDefault(defaultStreamName, null);

        assertNotNull("Stream " + defaultStreamName + " not found in model", stream);

        RecordSet recordSet = new RecordSet(stream, json);
        // should not throw exception
        recordSet.validate();

        JsonNode invalidRecord = loadData("recordset_invalid");

        recordSet = mapper.convertValue(invalidRecord, RecordSet.class);

        // should not throw exception
        recordSet.validate();

        recordSet.setStream(stream);

        // should throw an exception now, as there are additional channels
        recordSet.validate();

    }

//    @Test(expected = RaptorComponent.ValidationException.class)
    public void testParseRecordSet2() throws IOException {

        JsonNode resultset = loadData("resultset");
        JsonNode json = resultset.get(0);

        device.parse(jsonDevice);

        Stream stream = device.streams().getOrDefault(defaultStreamName, null);

        assertNotNull("Stream " + defaultStreamName + " not found in model", stream);

        RecordSet recordSet = new RecordSet(stream, json);

        // should throw an exception now, as there are additional channels
        recordSet.validate();

    }

}
