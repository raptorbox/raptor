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
import org.createnet.raptor.models.objects.ServiceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.createnet.raptor.http.events.DataEvent;
import org.createnet.raptor.http.service.EventEmitterService;
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
    public Collection<Stream> list(@PathParam("id") String id) {

        ServiceObject obj = loadObject(id);

        if (!auth.isAllowed(obj, Authorization.Permission.Read)) {
            throw new ForbiddenException("Cannot load stream list");
        }

        logger.debug("Load streams for object {}", obj.id);

        return obj.streams.values();
    }

    @GET
    @Path("{stream}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetch(
            @PathParam("id") String id,
            @PathParam("stream") String streamName,
            @QueryParam("limit") int limit,
            @QueryParam("offset") int offset
    ) {

        ServiceObject obj = loadObject(id);
        Stream stream = loadStream(streamName, obj);

        if (!auth.isAllowed(obj, Authorization.Permission.Pull)) {
            throw new ForbiddenException("Cannot access data");
        }

        if (!obj.settings.storeEnabled()) {
            return Response.noContent().build();
        }

        ResultSet data = indexer.fetchData(stream);

        logger.debug("Fetched {} records for stream {} in object {}", data.size(), streamName, obj.id);

        return Response.ok(data.toJson()).build();
    }

    @DELETE
    @Path("{stream}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("id") String id,
            @PathParam("stream") String streamName
    ) {

        ServiceObject obj = loadObject(id);
        Stream stream = loadStream(streamName, obj);

        if (!auth.isAllowed(obj, Authorization.Permission.Push)) {
            throw new ForbiddenException("Cannot delete data");
        }

        if (!obj.settings.storeEnabled()) {
            return Response.noContent().build();
        }

        storage.deleteData(stream);
        indexer.deleteData(stream);

        logger.debug("Delete all records for stream {} in object {}", streamName, obj.id);

        return Response.noContent().build();
    }

    @GET
    @Path("{stream}/lastUpdate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchLastUpdate(
            @PathParam("id") String id,
            @PathParam("stream") String streamName
    ) {

        ServiceObject obj = loadObject(id);
        Stream stream = loadStream(streamName, obj);

        if (!auth.isAllowed(obj, Authorization.Permission.Pull)) {
            throw new ForbiddenException("Cannot fetch data");
        }

        if (!obj.settings.storeEnabled()) {
            return Response.noContent().build();
        }

        RecordSet data = indexer.fetchLastUpdate(stream);

        logger.debug("Fetched lastUpdate record for stream {} in object {}", streamName, obj.id);

        if (data == null) {
            return Response.noContent().build();
        }

        return Response.ok(data.toJson()).build();
    }

    @PUT
    @Path("{stream}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response save(
            @PathParam("id") String id,
            @PathParam("stream") String streamName,
            RecordSet record
    ) {

        ServiceObject obj = loadObject(id);
        Stream stream = loadStream(streamName, obj);

        if (!auth.isAllowed(obj, Authorization.Permission.Push)) {
            throw new ForbiddenException("Cannot push data");
        }

        Authentication.UserInfo currentUser = auth.getUser();

        // set the stream, enforcing channels constrain on serialization
        // this avoid records that do not comply with the stored model
        record.userId = currentUser.getUserId();
        record.setStream(stream);
        record.validate();

        if (obj.settings.storeEnabled()) {

            logger.debug("Storing data for {} on {}", obj.id, stream.name);

            // save data
            storage.saveData(stream, record);
//            profiler.log("Saved record");

            // index data (with objectId and stream props)
            try {
                indexer.indexData(stream, record);
            } catch (Exception ex) {
                logger.error("Failed to index record for {}", obj.id);
                storage.deleteData(stream, record);
                throw new InternalServerErrorException();
            }

        } else {
            logger.debug("Skipped data storage for {}", obj.id);
        }

        emitter.trigger(EventEmitterService.EventName.push, new DataEvent(stream, record, auth.getAccessToken()));

        logger.debug("Received record for stream {} in object {}", streamName, obj.id);

        return Response.accepted().build();
    }

    @POST
    @Path("{stream}/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response search(
            @PathParam("id") String id,
            @PathParam("stream") String streamName,
            DataQuery query
    ) {

        ServiceObject obj = loadObject(id);
        Stream stream = loadStream(streamName, obj);

        if (!auth.isAllowed(obj, Authorization.Permission.Pull)) {
            throw new ForbiddenException("Cannot search data");
        }

        if (!obj.settings.storeEnabled()) {
            return Response.noContent().build();
        }

        ResultSet results = indexer.searchData(stream, query);

        logger.debug("Search data for stream {} in object {} has {} results", streamName, obj.id, results.size());

        return Response.ok().entity(results).build();
    }

}
