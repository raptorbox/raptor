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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

    public Device create() {

        Raptor raptor = Utils.getRaptor();

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

        ExecutorService pool = Executors.newCachedThreadPool();
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        final AtomicInteger jobCounter = new AtomicInteger(0);

        Raptor raptor = Utils.getRaptor();

        int devSize = 200;
        int recordSize = 1000;

        int len = jobCounter.addAndGet(devSize + devSize * recordSize);

        log.debug("Sending about {} request, hold tight and prepare to reboot :)", len);

        pool.submit(() -> {
            for (int i = 0; i < devSize; i++) {

                Device dev = create();

                log.debug("Created device {}", dev.name());
                int rem = jobCounter.decrementAndGet();
                log.debug("Remainging job {}", rem);

                final int v = i;
                pool.submit(() -> {
                    for (int i1 = 0; i1 < recordSize; i1++) {
                        RecordSet record = new RecordSet(dev.stream("values"))
                                .channel("temperature", i1 + 11.22 * Math.random())
                                .channel("light", i1 + 2.1 * Math.random())
                                .channel("pressure", i1 + 551 * Math.random());
                        log.debug("Parrallel push {}.{}", v, i1);
                        raptor.Stream().push(record);
                    }

                    int rrem = jobCounter.decrementAndGet();
                    log.debug("Remainging job {}", rrem);

                    return null;
                });

            }
        });
        
        pool.awaitTermination(1000, TimeUnit.SECONDS);
        
        Utils.waitUntil(1000, () -> {
            int cout = jobCounter.get();
            return cout == 0;
        });
        
        
    }

}
