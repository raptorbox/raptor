/*
 * Copyright 2017 l.
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
package org.createnet.raptor.models.objects;

import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author l
 */
public class DeviceNodeTest {

    public DeviceNodeTest() {
    }

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

    protected DeviceNode getNode(String id) {
        return new DeviceNode(new Device(id));
    }

    protected DeviceNode getNodes() {

        DeviceNode nodeA = getNode("A");
        DeviceNode nodeB = getNode("B");
        DeviceNode nodeC = getNode("C");
        DeviceNode nodeD = getNode("D");
        DeviceNode nodeE = getNode("E");

        nodeB.addChild(nodeC);
        nodeD.addChild(nodeE);

        nodeA.addChildren(Arrays.asList(nodeB, nodeD));

        return nodeA;
    }

    /**
     * Test of path method, of class DeviceNode.
     */
    @Test
    public void testPath() {

        DeviceNode node = getNodes();
        DeviceNode nodeE = node.getChild("D").get().getChild("E").get();

        String path = nodeE.path();
        assertEquals(path, "A/D/E");
    }

    /**
     * Test of path method, of class DeviceNode.
     */
    @Test
    public void testTree() {
        
        DeviceNode node = getNodes();
        DeviceNode nodeC = node.getChild("B").get().getChild("C").get();

        assertNotNull(nodeC);
    }

}
