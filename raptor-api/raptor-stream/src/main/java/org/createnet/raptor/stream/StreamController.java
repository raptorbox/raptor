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
package org.createnet.raptor.stream;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import org.createnet.raptor.common.client.ApiClientService;
import org.createnet.raptor.common.query.DataQueryBuilder;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.models.query.DataQuery;
import org.createnet.raptor.models.response.JsonErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
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
@RequestMapping(value = "/stream")
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
@Api(tags = {"Data"})
public class StreamController {

    final private Logger log = LoggerFactory.getLogger(StreamController.class);

    @Autowired
    private ApiClientService raptor;

    @Autowired
    private StreamEventPublisher streamPublisher;

    @Autowired
    private StreamService streamService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/{deviceId}/{streamId}"
    )
    @ApiOperation(
            value = "Save stream data",
            notes = "",
            nickname = "push"
    )
    @PreAuthorize("hasPermission(#deviceId, 'push')")
    public ResponseEntity<?> push(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("deviceId") String deviceId,
            @PathVariable("streamId") String streamId,
            @RequestBody RecordSet record
    ) {

        Device device = raptor.Inventory().load(deviceId);

        Stream stream = device.stream(streamId);
        if (stream == null) {
            return JsonErrorResponse.notFound("Stream not found");
        }
        record.setStream(stream);

        if (record.userId() == null) {
            record.userId(currentUser.getUuid());
        }

        if (!currentUser.isAdmin() && !record.userId().equals(currentUser.getUuid())) {
            record.userId(currentUser.getUuid());
        }
        
        try {
            record.validate();
        }
        catch(RaptorComponent.ValidationException ex) {
            return JsonErrorResponse.badRequest(ex.getMessage());
        }
        
        // save data!
        streamService.save(record);

        // notify of invocation       
        streamPublisher.push(record);

        return ResponseEntity.accepted().build();
    }

    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/{deviceId}/{streamId}"
    )
    @ApiOperation(
            value = "Remove all the stored data",
            notes = "",
            nickname = "delete"
    )
    @PreAuthorize("hasPermission(#deviceId, 'push')")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("deviceId") String deviceId,
            @PathVariable("streamId") String streamId
    ) {

        Device device = raptor.Inventory().load(deviceId);

        Stream stream = device.stream(streamId);
        if (stream == null) {
            return JsonErrorResponse.notFound("Stream not found");
        }

        // save data!
        streamService.deleteAll(stream);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{deviceId}/{streamId}"
    )
    @ApiOperation(
            value = "Retrieve all the stored stream data",
            notes = "",
            nickname = "list"
    )
    @PreAuthorize("hasPermission(#deviceId, 'pull')")
    public ResponseEntity<?> list(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("deviceId") String deviceId,
            @PathVariable("streamId") String streamId,
            Pageable pager
    ) {

        Device device = raptor.Inventory().load(deviceId);

        Stream stream = device.stream(streamId);
        if (stream == null) {
            return JsonErrorResponse.notFound("Stream not found");
        }

        List<RecordSet> records = streamService.list(stream, pager);

        return ResponseEntity.ok(records);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{deviceId}/{streamId}/lastUpdate"
    )
    @ApiOperation(
            value = "Retrieve the last record stored for a stream",
            notes = "",
            nickname = "lastUpdate"
    )
    @PreAuthorize("hasPermission(#deviceId, 'pull')")
    public ResponseEntity<?> lastUpdate(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("deviceId") String deviceId,
            @PathVariable("streamId") String streamId
    ) {

        Device device = raptor.Inventory().load(deviceId);

        Stream stream = device.stream(streamId);
        if (stream == null) {
            return JsonErrorResponse.notFound("Stream not found");
        }

        RecordSet record = streamService.lastUpdate(stream);

        if (record == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(record);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/{deviceId}/{streamId}"
    )
    @ApiOperation(
            value = "Retrieve data based on the search query",
            notes = "",
            nickname = "search"
    )
    @PreAuthorize("hasPermission(#deviceId, 'pull')")
    public ResponseEntity<?> search(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("deviceId") String deviceId,
            @PathVariable("streamId") String streamId,
            @RequestBody DataQuery query
    ) {

        Device device = raptor.Inventory().load(deviceId);

        Stream stream = device.stream(streamId);
        if (stream == null) {
            return JsonErrorResponse.notFound("Stream not found");
        }

        query.streamId(streamId);
        query.deviceId(deviceId);

        DataQueryBuilder qb = new DataQueryBuilder(query);
//        Pageable paging = qb.getPaging();
//        Predicate predicate = qb.getPredicate();
        Query q = qb.getQuery();

        ResultSet result = new ResultSet(stream);

        List<RecordSet> records = mongoTemplate.find(q, RecordSet.class);
        result.addAll(records);

//        Page<RecordSet> page = streamService.search(q, paging);
//        result.addAll(page.getContent());
        return ResponseEntity.ok(result);
    }

}
