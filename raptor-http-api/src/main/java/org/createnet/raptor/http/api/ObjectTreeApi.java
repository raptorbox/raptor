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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.models.objects.ServiceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.createnet.raptor.models.objects.ServiceObjectNode;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Api
@Path("/{id}/tree")
public class ObjectTreeApi extends AbstractApi {

    final private Logger logger = LoggerFactory.getLogger(ObjectTreeApi.class);

    @GET
    @Path("/{id}/tree")
    @ApiOperation(value = "Return the tree rapresentation of the object", notes = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok")
        ,
        @ApiResponse(code = 403, message = "Forbidden")
        ,
        @ApiResponse(code = 404, message = "Not Found")
    })
    public List<ServiceObject> tree(@PathParam("id") String id) {

        ServiceObject obj = loadObject(id);

        if (!auth.isAllowed(obj, Authorization.Permission.Read)) {
            throw new ForbiddenException("Cannot read object");
        }

        ServiceObjectNode node = tree.loadTree(obj);
        return node.objects();
    }

    @GET
    @ApiOperation(value = "Return the device children if any", notes = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok")
        ,
        @ApiResponse(code = 403, message = "Forbidden")
        ,
        @ApiResponse(code = 404, message = "Not Found")
    })
    public List<ServiceObject> children(@PathParam("id") String id) {

        ServiceObject obj = loadObject(id);

        if (!auth.isAllowed(obj, Authorization.Permission.Read)) {
            throw new ForbiddenException("Cannot read object");
        }

        List<ServiceObject> list = tree.getChildren(obj);
        return list;
    }

    @POST
    @ApiOperation(value = "Set a list of devices as children", notes = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok")
        ,
        @ApiResponse(code = 403, message = "Forbidden")
        ,
        @ApiResponse(code = 404, message = "Not Found")
    })
    public List<ServiceObject> setChildren(@PathParam("id") String id, final List<String> childrenIds) {

        ServiceObject parentObject = loadObject(id);

        if (!auth.isAllowed(parentObject, Authorization.Permission.Update)) {
            throw new ForbiddenException("Cannot update object");
        }

        List<ServiceObject> list = tree.setChildren(id, childrenIds);

        // @TODO: Move to event emitter
        List<ServiceObject> toSave = new ArrayList(list);
        toSave.add(parentObject);
        storage.saveObjects(toSave);

        return list;
    }

    @PUT
    @Path("{childrenId}")
    @ApiOperation(value = "Add a device as children", notes = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok")
        ,
        @ApiResponse(code = 403, message = "Forbidden")
        ,
        @ApiResponse(code = 404, message = "Not Found")
    })
    public List<ServiceObject> addChildren(@PathParam("id") String id, @PathParam("childrenId") String childrenId) {

        ServiceObject parentObject = loadObject(id);

        if (!auth.isAllowed(parentObject, Authorization.Permission.Update)) {
            throw new ForbiddenException("Cannot update object");
        }

        List<ServiceObject> list = tree.addChildren(id, Arrays.asList(childrenId));

        // @TODO: Move to event emitter
        List<ServiceObject> toSave = new ArrayList(list);
        toSave.add(parentObject);
        storage.saveObjects(toSave);

        return list;
    }

    @DELETE
    @Path("{childrenId}")
    @ApiOperation(value = "Remove a device from children list", notes = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok")
        ,
        @ApiResponse(code = 403, message = "Forbidden")
        ,
        @ApiResponse(code = 404, message = "Not Found")
    })
    public List<ServiceObject> removeChildren(@PathParam("id") String id, @PathParam("childrenId") String childrenId) {

        ServiceObject parentObject = loadObject(id);

        if (!auth.isAllowed(parentObject, Authorization.Permission.Update)) {
            throw new ForbiddenException("Cannot update object");
        }

        List<ServiceObject> list = tree.removeChildren(id, Arrays.asList(childrenId));

        // @TODO: Move to event emitter
        List<ServiceObject> toSave = new ArrayList(list);
        toSave.add(parentObject);
        storage.saveObjects(toSave);

        return list;

    }

}
