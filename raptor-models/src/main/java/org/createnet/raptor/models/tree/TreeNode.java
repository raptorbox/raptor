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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.createnet.raptor.models.acl.EntityType;
import org.createnet.raptor.models.acl.Owneable;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.RaptorContainer;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TreeNode extends RaptorContainer implements Owneable {
    
    public static final String separator = "/";

    public TreeNode() {
    }

    public TreeNode(String id) {
        this.id = id;
    }
    
    @Override
    public void validate() throws ValidationException {
        
        if(this.getId() == null) {
            throw new ValidationException("id cannot be null");
        }
        if(this.getType() == null) {
            throw new ValidationException("type cannot be null");
        }
        
    }

    @Override
    public void parse(String json) throws ParserException {
        try {
            TreeNode node = mapper.readValue(json, TreeNode.class);
            this.merge(node);
        } catch (IOException ex) {
            throw new ParserException(ex);
        }
    }
    
    @JsonIgnore
    @Override
    public String getOwnerId() {
        return getUserId();
    }
    
    @Id
    protected String id = UUID.randomUUID().toString();

    @Indexed
    protected String name = "";

    @Indexed
    protected EntityType type;

    @Indexed
    protected String userId;

    @Indexed
    protected String parentId = null;

    @Indexed
    protected long order = 0;

    protected Map<String, Object> properties = new HashMap();

    @Transient
    @JsonIgnore
    protected TreeNode parent = null;

    @Transient
    protected final List<TreeNode> children = new ArrayList<>();

    /**
     * Create a tree node from a Device
     *
     * @param device
     * @return
     */
    static public TreeNode create(Device device) {
        return new TreeNode()
                .id(device.id())
                .parentId(null)
                .userId(device.userId())
                .type(EntityType.device)
                .order(0);
    }

    /**
     * Create a group tree node
     *
     * @param name
     * @return
     */
    static public TreeNode create(String name) {
        return new TreeNode()
                .type(EntityType.tree)
                .name(name);
    }

    public TreeNode merge(TreeNode node) {
        this
                .id(node.getId())
                .name(node.getName())
                .parentId(node.getParentId())
                .userId(node.getUserId())
                .type(node.getType())
                .order(node.getOrder());

        this.children().addAll(node.children());
        this.parent(node.getParent());
        return this;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public EntityType getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public String getParentId() {
        return parentId;
    }

    public TreeNode getParent() {
        return parent;
    }

    public long getOrder() {
        return order;
    }

    public TreeNode id(String id) {
        this.id = id;
        return this;
    }

    public TreeNode name(String name) {
        this.name = name;
        return this;
    }

    public TreeNode type(EntityType type) {
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
        this.userId = user.getId();
        return this;
    }

    public TreeNode parent(TreeNode parent) {
        if (parent == null) {
            parentId(null);
            this.parent = null;
        } else {
            parentId(parent.getId());
            this.parent = parent;
        }
        return this;
    }

    @JsonGetter
    public List<TreeNode> children() {
        return children;
    }
    
    @JsonIgnore
    public boolean isDevice() {
        return getType().equals(EntityType.device);
    }

    @JsonIgnore
    public boolean isGroup() {
        return getType().equals(EntityType.group);
    }

    @JsonIgnore
    public boolean isUser() {
        return getType().equals(EntityType.user);
    }
    
    /**
     * Build a string with the path from root to the current node
     * Ensure the tree is fully loaded to avoid incomplete paths.
     * 
     * @return 
     */
    public String path() {
        
        ArrayList<String> path = new ArrayList(Arrays.asList(this.getId()));
        
        TreeNode p = this.getParent();
        if(p != null) {
            while(p != null) {
                String pid = p.getId();
                path.add(pid);
                p = p.getParent();
            }
        }
        
        Collections.reverse(path);
        return String.join(separator, path);
    }
}
