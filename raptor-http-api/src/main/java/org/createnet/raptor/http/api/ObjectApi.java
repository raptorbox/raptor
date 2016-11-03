/*
 * Copyright 2016 CREATE-NET
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
import java.net.URI;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.createnet.raptor.models.objects.ServiceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.createnet.raptor.search.query.impl.es.ObjectQuery;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Api
public class ObjectApi extends AbstractApi {

    final private Logger logger = LoggerFactory.getLogger(ObjectApi.class);

    @GET
    @ApiOperation(value = "List all the available devices definition", notes = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok")
        ,
    @ApiResponse(code = 403, message = "Forbidden")
    })
    public List<ServiceObject> list(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset
    ) {
        return objectManager.list(offset, limit);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a new device definition", notes = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok")
        ,
    @ApiResponse(code = 403, message = "Forbidden")
        ,
    @ApiResponse(code = 500, message = "Internal error")
    })
    public Response create(ServiceObject obj) {
        objectManager.create(obj);
        return Response.created(URI.create("/" + obj.id)).entity(obj.toJSON()).build();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update a device definition", notes = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok")
        ,
    @ApiResponse(code = 404, message = "Not Found")
        ,
    @ApiResponse(code = 403, message = "Forbidden")
    })
    public ServiceObject update(
            @PathParam("id") String id,
            ServiceObject obj
    ) {
        objectManager.update(id, obj);
        return obj;
    }

    @GET
    @Path("{id}")
    @ApiOperation(value = "Return a device definition", notes = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok")
        ,
    @ApiResponse(code = 403, message = "Forbidden")
    })
    public ServiceObject load(@PathParam("id") String id) {
        return objectManager.load(id);
    }

    @DELETE
    @Path("{id}")
    @ApiOperation(value = "Delete a device definition", notes = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok")
        ,
    @ApiResponse(code = 403, message = "Forbidden")
        ,
    @ApiResponse(code = 500, message = "Internal error")
    })
    public Response delete(@PathParam("id") String id) {
        objectManager.delete(id);
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Search for object definitions", notes = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok")
        ,
    @ApiResponse(code = 403, message = "Forbidden")
    })
    public List<ServiceObject> search(ObjectQuery query) {
        return objectManager.search(query);
    }

}
