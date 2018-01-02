/*
 * Copyright 2017 Luca Capra <luca.capra@fbk.eu>.
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
package org.createnet.raptor.models.payload;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.createnet.raptor.models.acl.EntityType;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.tree.TreeNode;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public class TreeNodePayload extends AbstractPayload {

    public String userId;
    public TreeNode node;
    public DispatcherPayload payload;

    public TreeNodePayload() {
    }

    public TreeNodePayload(TreeNode node, DispatcherPayload payload) {
        userId = node.getUserId();
        this.node = node;
        type = EntityType.tree;
        this.op = payload.getOp();
    }

    @Override
    public String toString() {
        try {
            return Device.getMapper().writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            throw new RaptorComponent.ParserException(ex);
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public TreeNode getNode() {
        return node;
    }

    public void setNode(TreeNode node) {
        this.node = node;
    }

    public DispatcherPayload getPayload() {
        return payload;
    }
    
}
