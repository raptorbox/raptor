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
package org.createnet.raptor.http.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Collection;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.indexer.query.impl.es.DataQuery;
import org.createnet.raptor.models.objects.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.objects.Stream;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Path("/{id}/streams")
@Produces(MediaType.APPLICATION_JSON)
@ApiResponses(value = {
    @ApiResponse(
            code = 200, 
            message = "Ok"
    ),
    @ApiResponse(
            code = 401,
            message = "Not authorized"
    ),
    @ApiResponse(
            code = 403, 
            message = "Forbidden"
    ),
    @ApiResponse(
            code = 500, 
            message = "Internal error"
    )
})
@Api(tags = { "Data" })
public class DataApi extends AbstractApi {

    final private Logger logger = LoggerFactory.getLogger(DataApi.class);

    @GET
    @ApiOperation(
            value = "List the channels defined for stream",
            notes = "",
            response = Stream.class,
            responseContainer = "Collection",
            nickname = "getStreamDefinition"
    )
    @ApiResponses(value = {})
    public Collection<Stream> list(@PathParam("id") String objectId) {

        Device obj = objectManager.load(objectId);

        if (!auth.isAllowed(obj, Authorization.Permission.Read)) {
            throw new ForbiddenException("Cannot load stream list");
        }
        
        return obj.streams.values();
    }

    @GET
    @Path("{stream}")
    @ApiOperation(
            value = "Return the last record saved for a stream",
            notes = "",
            response = RecordSet.class,
            nickname = "getLastUpdate"
    )
    @ApiResponses(value = {})    
    public Response fetchLastUpdate(
            @PathParam("id") String objectId,
            @PathParam("stream") String streamName
    ) {

        Stream stream = streamManager.load(objectId, streamName);
        Device obj = stream.getDevice();

        if (!auth.isAllowed(obj, Authorization.Permission.Pull)) {
            throw new ForbiddenException("Cannot fetch data");
        }        
        
        RecordSet data = streamManager.lastUpdate(stream);

        if (data == null) {
            return Response.noContent().build();
        }

        return Response.ok(data).build();
    }

    @DELETE
    @Path("{stream}")
    @ApiOperation(
            value = "Remove all the data stored for a stream",
            notes = "",
            nickname = "flushData"
    )
    @ApiResponses(value = {
        @ApiResponse(
                code = 204, 
                message = "No Content"
        )
    })
    public Response delete(
            @PathParam("id") String objectId,
            @PathParam("stream") String streamName
    ) {

        Stream stream = streamManager.load(streamName, objectId);
        Device obj = stream.getDevice();
        
        if (!auth.isAllowed(obj, Authorization.Permission.Push)) {
            throw new ForbiddenException("Cannot delete data");
        }
        
        streamManager.delete(stream);
        
        return Response.noContent().build();
    }

    @PUT
    @Path("{stream}")
    @ApiOperation(
            value = "Store a data record",
            notes = "",
            nickname = "pushData"
    )
    @ApiResponses(value = {
        @ApiResponse(
                code = 202, 
                message = "Accepted"
        )
    })
    public Response save(
            @PathParam("id") String objectId,
            @PathParam("stream") String streamName,
            RecordSet record
    ) {

        Stream stream = streamManager.load(objectId, streamName);
        Device obj = stream.getDevice();
        
        if (!auth.isAllowed(obj, Authorization.Permission.Push)) {
            throw new ForbiddenException("Cannot push data");
        }

        Authentication.UserInfo currentUser = auth.getUser();
        
        streamManager.push(stream, record, currentUser.getUserId());
        
        return Response.accepted().build();
    }

    @POST
    @Path("{stream}/search")
    @ApiOperation(
            value = "Search for records",
            notes = "",
            nickname = "searchData",
            response = RecordSet.class
    )
    @ApiResponses(value = {})
    public Response search(
            @PathParam("id") String objectId,
            @PathParam("stream") String streamName,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            DataQuery query
    ) {

        Stream stream = streamManager.load(objectId, streamName);
        Device obj = stream.getDevice();
        
        if (!auth.isAllowed(obj, Authorization.Permission.Pull)) {
            throw new ForbiddenException("Cannot search data");
        }
        
        ResultSet results = streamManager.search(stream, query, offset, limit);
        
        if(results == null) {
            return Response.noContent().build();
        }
        
        return Response.ok().entity(results).build();
    }

    @GET
    @Path("{stream}/list")
    @ApiOperation(
            value = "Return all the records sorted from the more recent",
            notes = "The maximum amount of data that can be returned per request is 1000 records. Use the pagination modifiers in the query (`offset` and `limit`) to control the data retrieval. Eg. `?limit=5000&offset=1000`",
            nickname = "fetchData",
            response = RecordSet.class
    )
    @ApiResponses(value = {})
    public Response fetch(
            @PathParam("id") String objectId,
            @PathParam("stream") String streamName,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset
    ) {
        
        Stream stream = streamManager.load(objectId, streamName);
        Device obj = stream.getDevice();
        
        if (!auth.isAllowed(obj, Authorization.Permission.Pull)) {
            throw new ForbiddenException("Cannot access data");
        }

        ResultSet result = streamManager.fetch(stream, offset, limit);
        
        if(result == null) {
            return Response.noContent().build();
        }
        
        return Response.ok(result).build();
    }

}
