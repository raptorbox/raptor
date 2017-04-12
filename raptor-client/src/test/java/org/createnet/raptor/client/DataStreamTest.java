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
package org.createnet.raptor.client;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.createnet.raptor.indexer.query.impl.es.DataQuery;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.Stream;
import org.elasticsearch.common.geo.GeoPoint;
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
        d.addStream("test", "location", "geo_point");

        Assert.assertTrue(d.getStream("test").channels.size() == 4);

        log.debug("Creating {} device", d.name);

        device = d;
    }

    @After
    public void tearDown() {
    }

    private Device createDevice(Device d) {
        return raptor.Device.create(d);
    }

    private Device createDevice() {
        return raptor.Device.create(device);
    }

    private List<RecordSet> createRecordSet(Stream stream, int length) throws InterruptedException {
        List<RecordSet> records = new ArrayList();
        for (int i = 0; i < length; i++) {

            RecordSet record = new RecordSet(stream);
            record.addRecord("number", i);
            record.addRecord("string", System.currentTimeMillis() % 2 == 0 ? "Hello world" : "See you later");
            record.addRecord("boolean", System.currentTimeMillis() % 2 == 0);
            record.addRecord("location", new GeoPoint(11.45, 45.11));

            long time = (long) (Instant.now().toEpochMilli() - (i * 100));

            log.debug("Set timestamp to {}", time);
            record.setTimestamp(new Date(time));

            records.add(record);
        }

        // wait indexing
        Thread.sleep(2500);
        return records;
    }

    @Test
    public void pushData() throws InterruptedException {

        log.debug("Push device data");

        Device dev = createDevice();
        Stream s = dev.getStream("test");

        List<RecordSet> records = createRecordSet(s, 1);

        records.parallelStream().forEach(record -> raptor.Stream.push(record));

    }

    @Test
    public void pullRecords() throws InterruptedException {

        log.debug("Pull device data");

        Device dev = createDevice();
        Stream s = dev.getStream("test");

        List<RecordSet> records = createRecordSet(s, 5);
        records.parallelStream().forEach(record -> raptor.Stream.push(record));

        // wait for indexing
        Thread.sleep(2500);

        ResultSet results = raptor.Stream.pull(s);
        Assert.assertTrue(results.size() > 0);

    }

    @Test
    public void searchByTimeRange() throws InterruptedException {

        log.debug("Search by time range");

        Device dev = createDevice();
        Stream s = dev.getStream("test");

        List<RecordSet> records = createRecordSet(s, 10);
        records.parallelStream().forEach(record -> raptor.Stream.push(record));

        // wait for indexing
        Thread.sleep(2500);

        DataQuery q = new DataQuery();
        q.timeRange(Instant.EPOCH);
        ResultSet results = raptor.Stream.search(s, q, 0, 10);

        Assert.assertEquals(10, results.size());
    }

    @Test
    public void searchByNumericRange() throws InterruptedException {

        log.debug("Search by numeric range");

        Device dev = createDevice();
        Stream s = dev.getStream("test");

        List<RecordSet> records = createRecordSet(s, 10);
        records.parallelStream().forEach(record -> raptor.Stream.push(record));

        // wait for indexing
        Thread.sleep(2500);

        DataQuery q = new DataQuery();
        q.range("channels.number", 0, 100);
        ResultSet results = raptor.Stream.search(s, q, 0, 10);

        Assert.assertEquals(10, results.size());
    }

}
