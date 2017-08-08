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
import org.createnet.raptor.models.payload.DevicePayload;
import org.createnet.raptor.models.payload.StreamPayload;
import org.createnet.raptor.models.payload.TreeNodePayload;
import org.createnet.raptor.models.tree.TreeNode;
import org.createnet.raptor.sdk.events.callback.DataCallback;
import org.createnet.raptor.sdk.events.callback.DeviceCallback;
import org.createnet.raptor.sdk.events.callback.TreeNodeCallback;
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
     * Subscribe to a tree event
     *
     * @param node
     * @param ev
     */
    public void subscribe(TreeNode node, TreeNodeCallback ev) {
        getEmitter().subscribe(node, (payload) -> {
            ev.callback(node, new TreeNodePayload(node, payload));
        });
    }

    /**
     * Subscribe to a data stream
     *
     * @param node
     * @param ev
     */
    public void subscribe(TreeNode node, DataCallback ev) {
        getEmitter().subscribe(node, (payload) -> {
            if(payload instanceof StreamPayload) {
                StreamPayload p = (StreamPayload) payload;
                ev.callback(p.device.stream(p.streamId), p.record);
            }
        });
    }

   /**
     * Subscribe only to device related events like update or delete
     *
     * @param node
     * @param ev
     */
    public void subscribe(TreeNode node, DeviceCallback ev) {
        getEmitter().subscribe(node, (payload) -> {
            switch (payload.getType()) {
                case device:
                    DevicePayload dp = (DevicePayload) payload;
                    ev.callback(dp.device, dp);
                    break;
            }
        });
    }    
    
    /**
     * Return the current tree structure for an user
     *
     * @return the
     */
    public List<TreeNode> list() {
        JsonNode json = getClient().get(Routes.TREE_LIST);
        List<TreeNode> list = Device.getMapper().convertValue(json, new TypeReference<List<TreeNode>>() {
        });
        return list;
    }

    /**
     * Return the whole tree structure for a node
     *
     * @param node
     * @return
     */
    public TreeNode tree(TreeNode node) {
        return tree(node.getId());
    }

    /**
     * Return the whole tree structure for a node
     *
     * @param nodeId
     * @return
     */
    public TreeNode tree(String nodeId) {
        JsonNode json = getClient().get(String.format(Routes.TREE_GET, nodeId));
        TreeNode tree = Device.getMapper().convertValue(json, TreeNode.class);
        return tree;
    }

    /**
     * Return the direct children of a node
     *
     * @param node
     * @return the
     */
    public List<TreeNode> children(TreeNode node) {
        JsonNode json = getClient().get(String.format(Routes.TREE_CHILDREN, node.getId()));
        List<TreeNode> list = Device.getMapper().convertValue(json, new TypeReference<List<TreeNode>>() {
        });
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
     * @param devices
     * @return
     */
    public List<TreeNode> add(Device... devices) {
        List<TreeNode> nodes = Arrays.asList(devices).stream()
                        .map((d) -> TreeNode.create(d))
                        .collect(Collectors.toList());
        add(null, nodes);
        return nodes;
    }

    /**
     * Add a node to a tree branch
     *
     * @param parent
     * @param node
     * @return
     */
    public TreeNode addChild(TreeNode parent, TreeNode node) {
        add(parent, Arrays.asList(node));
        return node;
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
        return nodes;
    }

    /**
     * Remove a node from a tree branch
     *
     * @param node
     */
    public void remove(TreeNode node) {
        String url = String.format(Routes.TREE_REMOVE, node.getId());
        getClient().delete(url);
    }

    /**
     * Remove a node and all children from a tree branch
     *
     * @param node
     */
    public void removeTree(TreeNode node) {
        String url = String.format(Routes.TREE_REMOVE_TREE, node.getId());
        getClient().delete(url);
    }

}
