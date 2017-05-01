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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.Utils;
import org.createnet.raptor.indexer.query.impl.es.DataQuery;
import org.createnet.raptor.indexer.query.impl.es.DataQueryBuilder;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.models.data.types.instances.DistanceUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Point;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public class DataStreamTest {

    final Logger log = LoggerFactory.getLogger(DataStreamTest.class);

    public static Raptor raptor;
    public static Device device;

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

        raptor = Utils.getRaptor();

        Device d = new Device();
        d.name = "data test " + System.currentTimeMillis();

        d.addStream("test", "string", "string");
        d.addStream("test", "number", "number");
        d.addStream("test", "boolean", "boolean");
        d.addStream("test", "string", "string");

        Assert.assertTrue(d.getStream("test").channels.size() == 4);

        log.debug("Creating {} device", d.name);

        device = d;
    }

    @After
    public void tearDown() {
    }

    private Device createDevice(Device d) {
        return raptor.Device().create(d);
    }

    private Device createDevice() {
        return raptor.Device().create(device);
    }

    private List<RecordSet> createRecordSet(Stream stream, int length) {
        List<RecordSet> records = new ArrayList();
        for (int i = 0; i < length; i++) {

            RecordSet record = new RecordSet(stream);
            record.addRecord("number", i);
            record.addRecord("string", System.currentTimeMillis() % 2 == 0 ? "Hello world" : "See you later");
            record.addRecord("boolean", System.currentTimeMillis() % 2 == 0);
            record.addRecord("location", new Point(11.45, 45.11));

            long time = (long) (Instant.now().toEpochMilli() - (i * 1000) - (Math.random() * 100));

            log.debug("Set timestamp to {}", time);
            record.setTimestamp(new Date(time));

            records.add(record);
        }

        // wait indexing
        Utils.waitFor(2500);
        return records;
    }
    
    private void pushRecords(Stream s, int len) {
        pushRecords(s, len, 2500);
    }
    
    private void pushRecords(Stream s, int len, int waitFor) {
        log.debug("Pushing {} records on {}", len, s.name);
        List<RecordSet> records = createRecordSet(s, len);
        records.parallelStream().forEach(record -> raptor.Stream().push(record));
        log.debug("Done, waiting for indexing {}millis", waitFor);
        Utils.waitFor(waitFor);
    }
    
    @Test
    public void pushData()  {

        log.debug("Push device data");

        Device dev = createDevice();
        Stream s = dev.getStream("test");

        pushRecords(s, 1);
        
        
    }

    @Test
    public void pullRecords() {

        log.debug("Pull device data");

        Device dev = createDevice();
        Stream s = dev.getStream("test");
        
        int qt = 5;
        pushRecords(s, qt);
        
        // wait for indexing
        Utils.waitFor(2500);

        ResultSet results = raptor.Stream().pull(s);
        Assert.assertEquals(qt, results.size());

    }

    @Test
    public void pullLastUpdate() {

        log.debug("Pull device last update");

        Device dev = createDevice();
        Stream s = dev.getStream("test");
        
        int qt = 1;
        pushRecords(s, qt, 10000);

        RecordSet record = raptor.Stream().lastUpdate(s);
        Assert.assertNotNull(record);
        
        Assert.assertEquals(11.45, record.location.getX(), 0);
    }
    
    @Test
    public void pullEmptyLastUpdate() {

        log.debug("Pull empty device last update");

        Device dev = createDevice();
        Stream s = dev.getStream("test");
        
        RecordSet record = raptor.Stream().lastUpdate(s);
        Assert.assertNull(record);
    }

    @Test
    public void searchByTimeRange()  {

        log.debug("Search by time range");

        Device dev = createDevice();
        Stream s = dev.getStream("test");
        
        int qt = 10;
        pushRecords(s, qt);

        DataQuery q = new DataQuery();
        q.timeRange(Instant.EPOCH);
        ResultSet results = raptor.Stream().search(s, q, 0, 10);

        Assert.assertEquals(10, results.size());
    }

    @Test
    public void searchByNumericRange()  {

        log.debug("Search by numeric range");

        Device dev = createDevice();
        Stream s = dev.getStream("test");

        List<RecordSet> records = createRecordSet(s, 10);
        records.parallelStream().forEach(record -> raptor.Stream().push(record));

        // wait for indexing
        Utils.waitFor(2500);

        DataQuery q = new DataQuery();
        q.range("number", 0, 100);
        ResultSet results = raptor.Stream().search(s, q, 0, 10);

        Assert.assertEquals(10, results.size());
    }
    
    @Test
    public void searchByDistance()  {

        log.debug("Search by distance");

        Device dev = createDevice();
        Stream s = dev.getStream("test");
        
        int qt = 10;
        pushRecords(s, qt);

        DataQuery q = new DataQuery();
        q.distance(new Point(11.45, 45.11), 10000, DistanceUnit.kilometers);
        ResultSet results = raptor.Stream().search(s, q, 0, 10);
        
        log.debug("Found {} records", results.size());
        Assert.assertTrue(results.size() > 0);
    }

    @Test
    public void searchByBoundingBox()  {

        log.debug("Search by bounding box");

        Device dev = createDevice();
        Stream s = dev.getStream("test");

        int qt = 10;
        pushRecords(s, qt);
        
        ResultSet results = raptor.Stream().search(s, DataQueryBuilder.boundingBox(new Point(12, 45), new Point(10, 44)), 0, 10);
        
        log.debug("Found {} records", results.size());
        Assert.assertTrue(results.size() > 0);
    }

    @Test
    public void dropData()  {

        log.debug("Drop data");

        Device dev = createDevice();
        Stream s = dev.getStream("test");

        int qt = 10;
        pushRecords(s, qt);
        
        ResultSet results = raptor.Stream().pull(s);
        
        log.debug("Found {} records", results.size());
        Assert.assertEquals(10, results.size());
        
        raptor.Stream().delete(s);
        
        // wait for indexing
        log.debug("Wait for indexing");
        Utils.waitFor(2500);
        
        results = raptor.Stream().pull(s);
        log.debug("Found {} records", results.size());
        Assert.assertEquals(0, results.size());
        
        
    }

}
