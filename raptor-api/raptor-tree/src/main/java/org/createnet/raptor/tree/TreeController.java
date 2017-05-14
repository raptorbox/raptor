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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.createnet.raptor.api.common.client.ApiClientService;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.response.JsonErrorResponse;
import org.createnet.raptor.models.tree.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
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
@RequestMapping(value = "/")
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

    @Autowired
    private MongoTemplate mongoTemplate;

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{nodeId}/tree"
    )
    @ApiOperation(
            value = "Return the whole tree a node belongs to",
            notes = "",
            response = TreeNode.class,
            responseContainer = "List",
            nickname = "tree"
    )
//    @PreAuthorize("hasPermission(#deviceId, 'read')")
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
//    @PreAuthorize("hasPermission(#deviceId, 'read')")
    public ResponseEntity<?> children(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("nodeId") String nodeId
    ) {
        List<TreeNode> children = treeService.children(nodeId);
        if(children == null) {
            return ResponseEntity.ok(new TreeNode[0]);
        }
        return ResponseEntity.ok(children);
    }

    @RequestMapping(
            method = RequestMethod.PUT,
            value = { "/{parentId}", "/" }
    )
    @ApiOperation(
            value = "Add items to the tree node",
            notes = "",
            nickname = "addNodes"
    )
//    @PreAuthorize("hasPermission(#deviceId, 'push')")
    public ResponseEntity<?> addNodes(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("parentId") Optional<String> optionalParentId,
            @RequestBody List<TreeNode> nodes
    ) {

        TreeNode parent = new TreeNode();
        if(optionalParentId.isPresent()) {
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
            
            if(node.getUserId() == null) {
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
            method = RequestMethod.POST,
            value = { "/" }
    )
    @ApiOperation(
            value = "Create a node of a tree",
            notes = "",
            nickname = "createNode"
    )
//    @PreAuthorize("hasPermission(#deviceId, 'push')")
    public ResponseEntity<?> createNode(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("parentId") Optional<String> optionalParentId,
            @RequestBody TreeNode raw
    ) {

        TreeNode node = new TreeNode();
        node.merge(raw);

        if(node.getUserId() == null) {
            node.user(currentUser);
        }
        if (!currentUser.isAdmin() && !node.getUserId().equals(currentUser.getUuid())) {
            node.user(currentUser);
        }

        treeService.save(node);
        log.debug("Added node {}", node.getId());

        return ResponseEntity.ok(node);
    }
    
//    @RequestMapping(
//            method = RequestMethod.DELETE,
//            value = "/{deviceId}/{streamId}"
//    )
//    @ApiOperation(
//            value = "Remove all the stored data",
//            notes = "",
//            nickname = "delete"
//    )
//    @PreAuthorize("hasPermission(#deviceId, 'push')")
//    public ResponseEntity<?> delete(
//            @AuthenticationPrincipal User currentUser,
//            @PathVariable("deviceId") String deviceId,
//            @PathVariable("streamId") String streamId
//    ) {
//
//        Device device = raptor.Inventory().load(deviceId);
//
//        Stream stream = device.getStream(streamId);
//        if (stream == null) {
//            return JsonErrorResponse.notFound("Stream not found");
//        }
//
//        // save data!
//        streamService.deleteAll(stream);
//
//        return ResponseEntity.ok().build();
//    }
//
//    @RequestMapping(
//            method = RequestMethod.GET,
//            value = "/{deviceId}/{streamId}/lastUpdate"
//    )
//    @ApiOperation(
//            value = "Retrieve the last record stored for a stream",
//            notes = "",
//            nickname = "lastUpdate"
//    )
//    @PreAuthorize("hasPermission(#deviceId, 'pull')")
//    public ResponseEntity<?> lastUpdate(
//            @AuthenticationPrincipal User currentUser,
//            @PathVariable("deviceId") String deviceId,
//            @PathVariable("streamId") String streamId
//    ) {
//
//        Device device = raptor.Inventory().load(deviceId);
//
//        Stream stream = device.getStream(streamId);
//        if (stream == null) {
//            return JsonErrorResponse.notFound("Stream not found");
//        }
//
//        RecordSet record = streamService.lastUpdate(stream);
//
//        if (record == null) {
//            return ResponseEntity.noContent().build();
//        }
//
//        return ResponseEntity.ok(record);
//    }
//
//    @RequestMapping(
//            method = RequestMethod.POST,
//            value = "/{deviceId}/{streamId}"
//    )
//    @ApiOperation(
//            value = "Retrieve data based on the search query",
//            notes = "",
//            nickname = "search"
//    )
//    @PreAuthorize("hasPermission(#deviceId, 'pull')")
//    public ResponseEntity<?> search(
//            @AuthenticationPrincipal User currentUser,
//            @PathVariable("deviceId") String deviceId,
//            @PathVariable("streamId") String streamId,
//            @RequestBody DataQuery query
//    ) {
//
//        Device device = raptor.Inventory().load(deviceId);
//
//        Stream stream = device.getStream(streamId);
//        if (stream == null) {
//            return JsonErrorResponse.notFound("Stream not found");
//        }
//
//        query.streamId(streamId);
//        query.deviceId(deviceId);
//
//        DataQueryBuilder qb = new DataQueryBuilder(query);
////        Pageable paging = qb.getPaging();
////        Predicate predicate = qb.getPredicate();
//        Query q = qb.getQuery();
//
//        ResultSet result = new ResultSet(stream);
//
//        List<RecordSet> records = mongoTemplate.find(q, RecordSet.class);
//        result.addAll(records);
//
////        Page<RecordSet> page = streamService.search(q, paging);
////        result.addAll(page.getContent());
//        return ResponseEntity.ok(result);
//    }
}
