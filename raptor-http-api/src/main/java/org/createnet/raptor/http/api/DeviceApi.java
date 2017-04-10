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
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
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
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.indexer.Indexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.createnet.raptor.indexer.query.impl.es.ObjectQuery;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Path("/")
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
@Api(tags = { "Inventory" })
public class DeviceApi extends AbstractApi {

    final private Logger logger = LoggerFactory.getLogger(DeviceApi.class);

    @GET
    @ApiOperation(
            value = "List all the available devices definition",
            notes = "",
            response = Device.class,
            responseContainer = "List",
            nickname = "listDevice"
    )
    @ApiResponses(value = {})
    public List<Device> list(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset
    ) {

        if (!auth.isAllowed(Authorization.Permission.List)) {
            throw new ForbiddenException("Cannot list objects");
        }

        return objectManager.list(auth.getUser().getUserId(), offset, limit);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Create a new device definition",
            notes = "",
            response = Device.class,
            nickname = "createDevice"
    )
    @ApiResponses(value = {})
    public Device create(Device obj) {

        if (!auth.isAllowed(Authorization.Permission.Create)) {
            throw new ForbiddenException("Cannot create object");
        }

        obj.userId = auth.getUser().getUserId();
        obj.validate();

        try {
            obj = objectManager.create(obj);
        } catch (Exception ex) {
            throw new InternalServerErrorException("Failed to create device", ex);
        }

        return obj;
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Update a device definition",
            notes = "",
            response = Device.class,
            nickname = "updateDevice"
    )
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Not Found")
    })
    public Device update(
            @PathParam("id") String objectId,
            Device obj
    ) {

        Device storedObj = objectManager.load(objectId);

        obj.id = objectId;

        if (!auth.isAllowed(storedObj, Authorization.Permission.Update)) {
            throw new ForbiddenException("Cannot update object");
        }

        obj = objectManager.update(obj);

//        boolean sync = syncObject(obj, Authentication.SyncOperation.UPDATE);
//        if (!sync) {
//            throw new InternalServerErrorException("Failed to sync device");
//        }

        return obj;
    }

    @GET
    @Path("{id}")
    @ApiOperation(
            value = "Return a device definition",
            notes = "",
            response = Device.class,
            nickname = "loadDevice"
    )
    @ApiResponses(value = {})
    public Device load(@PathParam("id") String id) {

        Device obj = objectManager.load(id);

        if (!auth.isAllowed(obj, Authorization.Permission.Read)) {
            throw new ForbiddenException("Cannot read object");
        }

        return obj;
    }

    @DELETE
    @Path("{id}")
    @ApiOperation(
            value = "Delete a device definition",
            notes = "",
            nickname = "deleteDevice"
    )
    @ApiResponses(value = {})
    public Response delete(@PathParam("id") String id) {

        Device obj = objectManager.load(id);

        if (!auth.isAllowed(obj, Authorization.Permission.Delete)) {
            throw new ForbiddenException("Cannot delete object");
        }

        objectManager.delete(id);

//        boolean sync = syncObject(obj, Authentication.SyncOperation.DELETE);
//        if (!sync) {
//            logger.error("Auth sync failed, aborting deletion of object {}", obj.id);
//            throw new InternalServerErrorException("Failed to sync device");
//        }

        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Search for object definitions",
            notes = "",
            response = Device.class,
            responseContainer = "List",
            nickname = "searchDevice"
    )
    @ApiResponses(value = {})
    public List<Device> search(ObjectQuery query) {

        if (!auth.isAllowed(Authorization.Permission.List)) {
            throw new ForbiddenException("Cannot search for objects");
        }

        //@TODO load accessible objects from auth service api!
        query.setUserId(auth.getUser().getUserId());

        List<Device> list = objectManager.search(query);

        return list;
    }

}
