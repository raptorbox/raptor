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
package org.createnet.raptor.models.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.createnet.raptor.models.auth.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@Document
public class TreeNode {

    public enum NodeType {
        group, user, device
    }

    @Id
    protected String id = UUID.randomUUID().toString();

    @Indexed
    protected NodeType type;

    @Indexed
    protected String userId;

    @Indexed
    protected String parentId = null;

    @Indexed
    protected long order = 0;

    protected Map<String, Object> properties = new HashMap();

    @Transient
    protected TreeNode parent = null;

    @Transient
    protected final List<TreeNode> children = new ArrayList<>();

    public TreeNode merge(TreeNode node) {
        return this
                .id(node.getId())
                .parentId(node.getParentId())
                .userId(node.getUserId())
                .type(node.getType())
                .order(node.getOrder());
    }

    public String getId() {
        return id;
    }

    public NodeType getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public String getParentId() {
        return parentId;
    }

    public long getOrder() {
        return order;
    }

    public TreeNode id(String id) {
        this.id = id;
        return this;
    }

    public TreeNode type(NodeType type) {
        this.type = type;
        return this;
    }

    public TreeNode userId(String userId) {
        this.userId = userId;
        return this;
    }

    public TreeNode parentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public TreeNode order(long order) {
        this.order = order;
        return this;
    }

    public Map<String, Object> properties() {
        return properties;
    }

    public TreeNode user(User user) {
        this.userId = user.getUuid();
        return this;
    }

    public TreeNode parent(TreeNode parent) {
        if(parent == null) {
            parentId(null);
            this.parent = null;
        }
        else {
            parentId(parent.getId());
            this.parent = parent;
        }
        return this;
    }

    public List<TreeNode> children() {
        return children;
    }

}
