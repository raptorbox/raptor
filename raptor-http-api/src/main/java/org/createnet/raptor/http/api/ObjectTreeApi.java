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
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.search.raptor.search.Indexer;
import org.createnet.raptor.config.exception.ConfigurationException;
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
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Not Found")
    })
    public List<ServiceObject> tree(@PathParam("id") String id) throws Storage.StorageException, RaptorComponent.ParserException, ConfigurationException, Authorization.AuthorizationException, Indexer.IndexerException, Authentication.AuthenticationException {

        ServiceObject obj = loadObject(id);

        if (!auth.isAllowed(obj, Authorization.Permission.Read)) {
            throw new ForbiddenException("Cannot read object");
        }

        ServiceObjectNode node = indexer.loadTree(obj);
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
    public List<ServiceObject> children(@PathParam("id") String id) throws Storage.StorageException, RaptorComponent.ParserException, ConfigurationException, Authorization.AuthorizationException, Indexer.IndexerException, Authentication.AuthenticationException {

        ServiceObject obj = loadObject(id);

        if (!auth.isAllowed(obj, Authorization.Permission.Read)) {
            throw new ForbiddenException("Cannot read object");
        }

        List<ServiceObject> list = indexer.getChildren(obj);
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
    public List<ServiceObject> setChildren(@PathParam("id") String id, final List<String> newChildren) throws Storage.StorageException, RaptorComponent.ParserException, ConfigurationException, Authorization.AuthorizationException, Indexer.IndexerException, Authentication.AuthenticationException, RaptorComponent.ValidationException {

        logger.debug("Setting {} new children", newChildren.size());

        List<String> toLoad = new ArrayList(newChildren);
        toLoad.add(id);

        List<ServiceObject> objs = indexer.getObjects(toLoad);
        List<ServiceObject> newChildrenObject = new ArrayList();

        ServiceObject parentObject = objs.stream().filter((ServiceObject o) -> {
            boolean isParent = o.id.equals(id);
            if (!isParent) {
                o.parentId = id;
                newChildrenObject.add(o);
            }
            return isParent;
        }).collect(Collectors.toList()).get(0);

        // save old and new children with modified properties
        final List<ServiceObject> toSave = new ArrayList<>(newChildrenObject);

        // reset old references
        List<ServiceObject> oldChildrenObject = indexer.getChildren(parentObject);
        logger.debug("Previous children size: {}", oldChildrenObject.size());
        oldChildrenObject.stream().forEach(o -> {
            if (!newChildren.contains(o.id)) {
                o.parentId = null;
                toSave.add(o);
            }
        });

        if (parentObject == null) {
            throw new NotFoundException("Cannot load parent object");
        }

        if (!auth.isAllowed(parentObject, Authorization.Permission.Update)) {
            throw new ForbiddenException("Cannot update object");
        }

        indexer.saveObjects(toSave);
        storage.saveObjects(toSave);

        indexer.setChildrenList(parentObject, newChildren);

        return children(id);
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
    public List<ServiceObject> addChildren(@PathParam("id") String id, @PathParam("childrenId") String childrenId) throws Storage.StorageException, RaptorComponent.ParserException, ConfigurationException, Authorization.AuthorizationException, Indexer.IndexerException, Authentication.AuthenticationException, RaptorComponent.ValidationException {

        List<String> children = indexer.getChildrenList(id);

        if (children.contains(childrenId)) {
            return children(id);
        }

        children.add(childrenId);
        return setChildren(id, children);
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
    public List<ServiceObject> removeChildren(@PathParam("id") String id, @PathParam("childrenId") String childrenId) throws Storage.StorageException, RaptorComponent.ParserException, ConfigurationException, Authorization.AuthorizationException, Indexer.IndexerException, Authentication.AuthenticationException, RaptorComponent.ValidationException {
        List<String> children = indexer.getChildrenList(id);
        if (!children.contains(childrenId)) {
            return children(id);
        }
        children.remove(childrenId);
        return setChildren(id, children);
    }

}
