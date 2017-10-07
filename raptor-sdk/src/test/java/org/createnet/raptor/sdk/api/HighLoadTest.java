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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.Utils;
import org.createnet.raptor.models.objects.Device;
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
public class HighLoadTest {

    // Ensure those values are compatible with the setup of CI
    int poolSize = 5;
    int devSize = 10;
    int recordSize = 100;

    final Logger log = LoggerFactory.getLogger(HighLoadTest.class);

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

    public Device create(Raptor raptor) {

        Device dev = new Device();
        dev.name("highload " + (System.currentTimeMillis() * Math.random()));

        dev.addStream("position", "location", "string");
        dev
                .addStream("values", "temperature", "number")
                .addChannel("light", "number")
                .addChannel("pressure", "number");

        dev.validate();
        raptor.Inventory().create(dev);

        log.debug("Device created {}", dev.id());
        assertNotNull(dev.id());

        return dev;
    }

    @Test
    public void testPush() throws InterruptedException {

        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        
        final AtomicInteger jobCounter = new AtomicInteger(0);

        int len = jobCounter.addAndGet(devSize * recordSize);

        log.debug("Sending about {} request, hold tight and prepare to reboot :)", len);

        for (int i = 0; i < devSize; i++) {

            Raptor r = Utils.createNewInstance();
            Device dev = create(r);
            log.debug("Created device {}", dev.name());

            for (int i1 = 0; i1 < recordSize; i1++) {
                final int v = i;
                final int v1 = i1;
                pool.submit(() -> {
                    RecordSet record = new RecordSet(dev.stream("values"))
                            .channel("temperature", v1 + 11.22 * Math.random())
                            .channel("light", v1 + 2.1 * Math.random())
                            .channel("pressure", v1 + 551 * Math.random());
                    
                    log.debug("Parrallel push {}.{}", v, v1);
                    r.Stream().push(record);
                    
                    int rrem = jobCounter.decrementAndGet();
                    log.debug("Remainging job {}", rrem);
                    
                    return null;
                });
            }

        }

        pool.shutdown();

        Utils.waitUntil(25 + (devSize * (recordSize / 10)), () -> {
            int cout = jobCounter.get();
            log.debug("Work status {}, pool done {}, finished {}", cout, pool.isTerminated(), (pool.isTerminated() && cout == 0));
            return !(pool.isTerminated() && cout == 0);
        });

    }
    

    @Test
    public void testParallelPush() throws InterruptedException {
        
        int jobSize = 2;
        
        ExecutorService pool = Executors.newFixedThreadPool(jobSize);

        for (int i = 0; i < jobSize; i++) {
            final int v = i;
            pool.submit(() -> {
                testPush();
                return null;
            });

        }

        pool.shutdown();

        Utils.waitUntil(jobSize * (25 + (devSize * (recordSize / 10))), () -> {
            return !(pool.isTerminated());
        });

    }
    
    
}
