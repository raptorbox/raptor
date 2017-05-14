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

import com.querydsl.core.types.Predicate;
import java.util.List;
import org.createnet.raptor.models.tree.QTreeNode;
import org.createnet.raptor.models.tree.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class TreeService {

    @Autowired
    private TreeRepository repository;

    /** 
     * Return a single node by id
     * 
     * @param id
     * @return
     */
    public TreeNode get(String id) {
        return repository.findOne(id);
    }
    
    /**
     * Return a list of nodes by ids
     * @param ids
     * @return
     */
    public Iterable<TreeNode> get(Iterable<String> ids) {
        return repository.findAll(ids);
    }

    /**
     * Return the direct leaves of a node
     *
     * @param node
     * @return
     */
    public List<TreeNode> children(TreeNode node) {

        QTreeNode q = new QTreeNode("node");

        Predicate p = q.parentId.eq(node.getId());
        List<TreeNode> children = repository.findAll(p);

        children.parallelStream()
                .forEach((n) -> {
                    n.parent(node);
                });

        return children;
    }

    /**
     * Return the direct leaves of a node
     *
     * @param id
     * @return
     */
    public List<TreeNode> children(String id) {
        TreeNode node = get(id);
        if (node == null) {
            return null;
        }        
        return children(node);
    }

    /**
     * Return the subtree of the node
     *
     * @param node
     * @return
     */
    public TreeNode subtree(TreeNode node) {

        // find the direct children
        List<TreeNode> children = children(node);

        children.forEach((n) -> {
            subtree(n);
        });

        node.children().addAll(children);

        return node;
    }

    /**
     * Return the root of the node
     *
     * @param node
     * @return
     */
    public TreeNode root(TreeNode node) {

        if (node == null) {
            return null;
        }

        String parentId = node.getParentId();
        if (parentId == null) {
            return node;
        }

        TreeNode parent = get(parentId);
        return root(parent);
    }

    /**
     * Return the whole tree a node belongs to
     *
     * @param node
     * @return
     */
    public TreeNode tree(TreeNode node) {

        if (node == null) {
            return null;
        }

        TreeNode root = root(node);
        return subtree(root);
    }

    /**
     * Return the subtree of the node
     *
     * @param id
     * @return
     */
    public TreeNode subtree(String id) {
        TreeNode node = get(id);
        if (node == null) {
            return null;
        }
        return subtree(node);
    }

    public TreeNode root(String id) {
        TreeNode node = get(id);
        if (node == null) {
            return null;
        }
        return root(node);
    }

    public TreeNode tree(String id) {
        TreeNode node = get(id);
        if (node == null) {
            return null;
        }
        return tree(node);
    }

    public TreeNode save(TreeNode node) {
        return repository.save(node);
    }

}
