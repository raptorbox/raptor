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

import org.createnet.raptor.sdk.Routes;
import org.createnet.raptor.sdk.AbstractClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.tree.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods to interact with Raptor API
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class TreeClient extends AbstractClient {

    public TreeClient(Raptor container) {
        super(container);
    }

    final static Logger logger = LoggerFactory.getLogger(TreeClient.class);

    /**
     * Return the current tree structure for an user
     *
     * @return the 
     */
    public List<TreeNode> list() {
        JsonNode json = getClient().get(Routes.TREE_LIST);
        List<TreeNode> list = Device.getMapper().convertValue(json, new TypeReference<List<TreeNode>>() {});
        return list;
    }

    /**
     * Return the whole tree structure for a node
     *
     * @param node
     * @return the 
     */
    public TreeNode tree(TreeNode node) {
        JsonNode json = getClient().get(String.format(Routes.TREE_GET, node.getId()));
        TreeNode tree = Device.getMapper().convertValue(json, TreeNode.class);
        return node.merge(tree);
    }

    /**
     * Return the direct children of a node
     *
     * @param node
     * @return the 
     */
    public List<TreeNode> children(TreeNode node) {
        JsonNode json = getClient().get(String.format(Routes.TREE_CHILDREN, node.getId()));
        List<TreeNode> list = Device.getMapper().convertValue(json, new TypeReference<List<TreeNode>>() {});
        return list;
    }

    /**
     * Add nodes to a tree branch
     *
     * @param parent
     * @param devices
     * @return 
     */
    public List<TreeNode> add(TreeNode parent, Device... devices) {
        return add(parent, 
                Arrays.asList(devices).stream()
                        .map((d) -> TreeNode.create(d))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Add nodes to a tree branch
     *
     * @param devices
     * @return 
     */
    public List<TreeNode> add(Device... devices) {
        return add(null, 
                Arrays.asList(devices).stream()
                        .map((d) -> TreeNode.create(d))
                        .collect(Collectors.toList())
        );
    }
    
    /**
     * Create a node of a tree
     *
     * @param node
     * @return 
     */
    public TreeNode create(TreeNode node) {
        JsonNode json = getClient().post(Routes.TREE_CREATE, toJsonNode(node));
        TreeNode node1 = Device.getMapper().convertValue(json, TreeNode.class);
        return node.merge(node1);
    }
    
    /**
     * Add nodes to a tree branch
     *
     * @param parent
     * @param nodes
     * @return 
     */
    public List<TreeNode> add(TreeNode parent, List<TreeNode> nodes) {
        
        String url = String.format(Routes.TREE_ADD, parent == null ? "" : parent.getId());
        JsonNode json = getClient().put(url, toJsonNode(nodes));
        
        List<TreeNode> list = Device.getMapper().convertValue(json, new TypeReference<List<TreeNode>>() {});

        return list;
    }
    
    /**
     * Remove node from a tree branch
     *
     * @param node
     */
    public void remove(TreeNode node) {
        String url = String.format(Routes.TREE_REMOVE, node.getId());
        getClient().delete(url);
    }

}
