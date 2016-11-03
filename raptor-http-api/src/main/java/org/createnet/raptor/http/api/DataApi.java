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

import java.util.Collection;
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
import org.createnet.raptor.events.Event.EventName;
import org.createnet.raptor.models.objects.ServiceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.createnet.raptor.events.type.DataEvent;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.search.query.impl.es.DataQuery;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Path("/{id}/streams")
public class DataApi extends AbstractApi {

    final private Logger logger = LoggerFactory.getLogger(DataApi.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Stream> list(@PathParam("id") String objectId) {

        ServiceObject obj = objectManager.load(objectId);

        if (!auth.isAllowed(obj, Authorization.Permission.Read)) {
            throw new ForbiddenException("Cannot load stream list");
        }
        
        return obj.streams.values();
    }

    @GET
    @Path("{stream}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchLastUpdate(
            @PathParam("id") String objectId,
            @PathParam("stream") String streamName
    ) {

        Stream stream = streamManager.load(objectId, streamName);
        ServiceObject obj = stream.getServiceObject();

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
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("id") String objectId,
            @PathParam("stream") String streamName
    ) {

        Stream stream = streamManager.load(streamName, objectId);
        ServiceObject obj = stream.getServiceObject();
        
        if (!auth.isAllowed(obj, Authorization.Permission.Push)) {
            throw new ForbiddenException("Cannot delete data");
        }
        
        streamManager.delete(stream);
        
        return Response.noContent().build();
    }

    @PUT
    @Path("{stream}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response save(
            @PathParam("id") String objectId,
            @PathParam("stream") String streamName,
            RecordSet record
    ) {

        Stream stream = streamManager.load(objectId, streamName);
        ServiceObject obj = stream.getServiceObject();
        
        if (!auth.isAllowed(obj, Authorization.Permission.Push)) {
            throw new ForbiddenException("Cannot push data");
        }

        Authentication.UserInfo currentUser = auth.getUser();
        
        streamManager.push(stream, record, currentUser.getUserId());
        
        return Response.accepted().build();
    }

    @POST
    @Path("{stream}/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response search(
            @PathParam("id") String objectId,
            @PathParam("stream") String streamName,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            DataQuery query
    ) {


        Stream stream = streamManager.load(objectId, streamName);
        ServiceObject obj = stream.getServiceObject();
        
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetch(
            @PathParam("id") String objectId,
            @PathParam("stream") String streamName,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset
    ) {

        
        Stream stream = streamManager.load(objectId, streamName);
        ServiceObject obj = stream.getServiceObject();
        
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
