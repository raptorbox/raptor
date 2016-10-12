/*
 * Copyright 2016 l.
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
public class ServiceObjectNodeTest {

    public ServiceObjectNodeTest() {
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

    protected ServiceObjectNode getNode(String id) {
        return new ServiceObjectNode(new ServiceObject(id));
    }

    protected ServiceObjectNode getNodes() {

        ServiceObjectNode nodeA = getNode("A");
        ServiceObjectNode nodeB = getNode("B");
        ServiceObjectNode nodeC = getNode("C");
        ServiceObjectNode nodeD = getNode("D");
        ServiceObjectNode nodeE = getNode("E");

        nodeB.addChild(nodeC);
        nodeD.addChild(nodeE);

        nodeA.addChildren(Arrays.asList(nodeB, nodeD));

        return nodeA;
    }

    /**
     * Test of path method, of class ServiceObjectNode.
     */
    @Test
    public void testPath() {

        ServiceObjectNode node = getNodes();
        ServiceObjectNode nodeE = node.getChild("D").get().getChild("E").get();

        String path = nodeE.path();
        assertEquals(path, "A/D/E");
    }

    /**
     * Test of path method, of class ServiceObjectNode.
     */
    @Test
    public void testTree() {
        
        ServiceObjectNode node = getNodes();
        ServiceObjectNode nodeC = node.getChild("B").get().getChild("C").get();

        assertNotNull(nodeC);
    }

}
