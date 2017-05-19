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

import java.util.Arrays;
import java.util.List;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.Utils;
import org.createnet.raptor.models.tree.TreeNode;
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
public class TreeTest {

    final Logger log = LoggerFactory.getLogger(TreeTest.class);

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

    @Test
    public void list() {

        Raptor raptor = Utils.createNewInstance();

        log.debug("List trees");

        List<TreeNode> list = raptor.Tree().list();

        log.debug("found {} trees", list.size());
        assertNotNull(list);
        assertEquals(0, list.size());

        TreeNode node1 = TreeNode.create("Root1");
        TreeNode node2 = TreeNode.create("Root2");
        raptor.Tree().create(node1);
        raptor.Tree().create(node2);

        list = raptor.Tree().list();
        log.debug("found {} trees", list.size());
        assertNotNull(list);
        assertTrue(2 == list.size());

    }

    @Test
    public void create() {

        Raptor raptor = Utils.createNewInstance();

        log.debug("create tree");

        TreeNode node1 = TreeNode.create("Root1");
        raptor.Tree().create(node1);

        TreeNode child1 = TreeNode.create("child1");
        TreeNode child2 = TreeNode.create("child2");
        
        raptor.Tree().add(node1, Arrays.asList(child1));
        raptor.Tree().add(child1, Arrays.asList(child2));
        
        List<TreeNode> nodes = raptor.Tree().list();
        
        assertEquals(1, nodes.size());
        
        TreeNode tree = raptor.Tree().tree(node1);
        
        assertEquals(tree, node1);
        assertEquals(1, tree.children().size());
        assertEquals("child1", tree.children().get(0).getName());
        assertEquals("child2", tree.children().get(0).children().get(0).getName());
        
    }

    @Test
    public void delete() {

        Raptor raptor = Utils.createNewInstance();

        log.debug("create tree");

        TreeNode node1 = TreeNode.create("Root1");
        raptor.Tree().create(node1);

        TreeNode child1 = TreeNode.create("child1");
        TreeNode child2 = TreeNode.create("child2");
        
        raptor.Tree().add(node1, Arrays.asList(child1, child2));
        
        TreeNode tree = raptor.Tree().tree(node1);
        
        assertEquals(tree, node1);
        assertEquals(2, tree.children().size());
        
        raptor.Tree().remove(tree.children().get(0));
        
        List<TreeNode> children = raptor.Tree().children(node1);
        assertEquals(1, children.size());
        
        
        tree = raptor.Tree().tree(node1);
        assertEquals(1, tree.children().size());

    }

}
