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
package org.createnet.raptor.events.type;

import org.createnet.raptor.events.AbstractEvent;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.createnet.raptor.models.tree.TreeNode;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class TreeNodeEvent extends AbstractEvent {

    final private TreeNode node;
    final private DispatcherPayload payload;

    public TreeNodeEvent(TreeNode node, DispatcherPayload payload) {
        this.node = node;
        this.payload = payload;
        
    }

    public TreeNode getNode() {
        return node;
    }

    public DispatcherPayload getPayload() {
        return payload;
    }

}
