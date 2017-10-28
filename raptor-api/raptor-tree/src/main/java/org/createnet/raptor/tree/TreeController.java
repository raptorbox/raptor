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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.createnet.raptor.common.client.ApiClientService;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.response.JsonErrorResponse;
import org.createnet.raptor.models.tree.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@RestController
@RequestMapping(value = "/tree")
@ApiResponses(value = {
    @ApiResponse(
            code = 200,
            message = "Ok"
    )
    ,
    @ApiResponse(
            code = 204,
            message = "No content"
    )
    ,
    @ApiResponse(
            code = 202,
            message = "Accepted"
    )
    ,
    @ApiResponse(
            code = 401,
            message = "Not authorized"
    )
    ,
    @ApiResponse(
            code = 403,
            message = "Forbidden"
    )
    ,
    @ApiResponse(
            code = 500,
            message = "Internal error"
    )
})
@Api(tags = {"Inventory", "Tree"})
public class TreeController {

    final private Logger log = LoggerFactory.getLogger(TreeController.class);

    @Autowired
    private ApiClientService raptor;

    @Autowired
    private TreeService treeService;

    @RequestMapping(
            method = RequestMethod.POST
    )
    @ApiOperation(
            value = "Create a node of a tree",
            notes = "",
            nickname = "create"
    )
    @PreAuthorize("@raptorSecurity.can(principal, 'tree', 'create')")
    public ResponseEntity<?> create(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("parentId") Optional<String> optionalParentId,
            @RequestBody TreeNode raw
    ) {

        TreeNode node = new TreeNode();
        node.merge(raw);

        if (node.getUserId() == null) {
            node.user(currentUser);
        }
        if (!currentUser.isAdmin() && !node.getUserId().equals(currentUser.getUuid())) {
            node.user(currentUser);
        }

        treeService.save(node);
        log.debug("Added node {}", node.getId());

        return ResponseEntity.ok(node);
    }

    @RequestMapping(
            method = RequestMethod.GET
    )
    @ApiOperation(
            value = "List all trees",
            notes = "",
            response = TreeNode.class,
            responseContainer = "List",
            nickname = "list"
    )
    @PreAuthorize("@raptorSecurity.list(principal, 'tree')")
    public ResponseEntity<?> list(
            @AuthenticationPrincipal User currentUser
    ) {
        TreeNode root = (new TreeNode()).id(null).user(currentUser);
        List<TreeNode> roots = treeService.children(root);
        List<TreeNode> nodes = roots.stream().map((n) -> treeService.tree(n)).collect(Collectors.toList());
        return ResponseEntity.ok(nodes);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{nodeId}"
    )
    @ApiOperation(
            value = "Return the whole tree a node belongs to",
            notes = "",
            response = TreeNode.class,
            responseContainer = "List",
            nickname = "tree"
    )
    @PreAuthorize("@raptorSecurity.can(principal, 'tree', 'read', #nodeId)")
    public ResponseEntity<?> tree(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("nodeId") String nodeId
    ) {
        TreeNode tree = treeService.tree(nodeId);
        return ResponseEntity.ok(tree);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{nodeId}/children"
    )
    @ApiOperation(
            value = "Return the children for this node",
            notes = "",
            response = TreeNode.class,
            responseContainer = "List",
            nickname = "children"
    )
    @PreAuthorize("@raptorSecurity.can(principal, 'tree', 'read', #nodeId)")
    public ResponseEntity<?> children(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("nodeId") String nodeId
    ) {
        List<TreeNode> children = treeService.children(nodeId);
        if (children == null) {
            return ResponseEntity.ok(new TreeNode[0]);
        }
        return ResponseEntity.ok(children);
    }

    @RequestMapping(
            method = RequestMethod.PUT,
            value = {"/{parentId}", "/"}
    )
    @ApiOperation(
            value = "Add items to the tree node",
            notes = "",
            nickname = "add"
    )
    @PreAuthorize("@raptorSecurity.can(principal, 'tree', 'create', #parentId)")
    public ResponseEntity<?> add(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("parentId") Optional<String> optionalParentId,
            @RequestBody List<TreeNode> nodes
    ) {

        TreeNode parent = new TreeNode();
        if (optionalParentId.isPresent()) {
            TreeNode storedParent = treeService.get(optionalParentId.get());
            if (storedParent == null) {
                return JsonErrorResponse.notFound("Node not found");
            }
            parent.merge(storedParent);
        }

        nodes.stream().forEach((raw) -> {

            TreeNode node = treeService.get(raw.getId());
            if (node == null) {
                node = new TreeNode();
            }

            node.merge(raw);
            node.parent(parent);

            if (node.getUserId() == null) {
                node.user(currentUser);
            }
            if (!currentUser.isAdmin() && !node.getUserId().equals(currentUser.getUuid())) {
                node.user(currentUser);
            }

            treeService.save(node);
            log.debug("Added children {}", node.getId());
        });

        return ResponseEntity.accepted().build();
    }

    @RequestMapping(
            method = RequestMethod.DELETE,
            value = {"/{nodeId}"}
    )
    @ApiOperation(
            value = "Delete a node from a tree",
            notes = "",
            nickname = "delete"
    )
    @PreAuthorize("@raptorSecurity.can(principal, 'tree', 'delete', #nodeId)")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("nodeId") String nodeId
    ) {

        TreeNode node = treeService.get(nodeId);

        if (node == null) {
            return JsonErrorResponse.notFound();
        }

        treeService.delete(node);
        log.debug("Deleted node {}", node.getId());

        return ResponseEntity.accepted().build();
    }

}
