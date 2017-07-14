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
package org.createnet.raptor.tree;

import org.createnet.raptor.api.common.dispatcher.RaptorMessageHandler;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.payload.ActionPayload;
import org.createnet.raptor.models.payload.DevicePayload;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.createnet.raptor.models.payload.StreamPayload;
import org.createnet.raptor.models.tree.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class TreeMessageHandler implements RaptorMessageHandler {

    final Logger log = LoggerFactory.getLogger(TreeMessageHandler.class);

    @Autowired
    TreeService treeService;

    @Autowired
    private TreeNodeEventPublisher treeNodePublisher;

    @Override
    public void handle(DispatcherPayload dispatcherPayload, MessageHeaders headers) {

        switch (dispatcherPayload.getType()) {
//            case tree:
//                handleTreeNode((TreeNodePayload) dispatcherPayload);
//                break;
            case device:
                handleDevice((DevicePayload) dispatcherPayload);
                break;
            case action:
                handleAction((ActionPayload) dispatcherPayload);
                break;
            case stream:
                handleStream((StreamPayload) dispatcherPayload);
                break;
        }

    }
    
    protected void handleAction(ActionPayload payload) {
        Device device = payload.getDevice();
        TreeNode node = treeService.get(device.getId());
        if (node != null) {
            notifyParent(node, payload);
        }
    }
    
    protected void handleStream(StreamPayload payload) {
        Device device = payload.getDevice();
        TreeNode node = treeService.get(device.getId());
        if (node != null) {
            notifyParent(node, payload);
        }
    }

    protected void handleDevice(DevicePayload payload) {

        TreeNode node = treeService.get(payload.device.id());

        if (node == null) {
            log.debug("Cannot load node {}", payload.device.id());
            return;
        }

        switch (payload.op) {
            case delete:
                log.debug("Drop node {}", payload.device.id());
                treeService.delete(payload.device.id());
                break;
            case update:
                log.debug("Update node {}", payload.device.id());
                node.name(payload.device.name());
                node = treeService.save(node);
                break;
        }

        notifyParent(node, payload);
        
    }

    protected void notifyParent(TreeNode node, DispatcherPayload payload) {
        TreeNode parents = treeService.parents(node);
        TreeNode parent = parents.getParent();
        while (parent != null) {
            log.debug("Notifiyng {} ({})", parent.getId(), parent.path());
            treeNodePublisher.notify(parent, payload);
            parent = parent.getParent();
        }
    }

}
