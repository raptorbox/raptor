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

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.createnet.raptor.db.Storage;
import org.createnet.search.raptor.search.Indexer;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.createnet.raptor.http.events.DataEvent;
import org.createnet.raptor.http.service.EventEmitterService;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.exception.RecordsetException;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.search.raptor.search.query.impl.es.DataQuery;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Path("/{id}/streams")
public class DataApi extends AbstractApi {

  final private Logger logger = LoggerFactory.getLogger(DataApi.class);

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Stream> list(@PathParam("id") String id)
          throws Storage.StorageException, RaptorComponent.ParserException, ConfigurationException, Authorization.AuthorizationException, Authentication.AuthenticationException, IOException {

    ServiceObject obj = loadObject(id);

    if (!auth.isAllowed(id, Authorization.Permission.Read)) {
      throw new ForbiddenException("Cannot fetch data");
    }
    
    logger.debug("Load streams for object {}", obj.id);

    return obj.streams.values();
  }

  @GET
  @Path("{stream}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response fetch(
          @PathParam("id") String id,
          @PathParam("stream") String streamName
  ) throws RaptorComponent.ParserException, ConfigurationException, Storage.StorageException, RaptorComponent.ValidationException, Authorization.AuthorizationException, Authentication.AuthenticationException, JsonProcessingException, RecordsetException, IOException {

    ServiceObject obj = loadObject(id);
    Stream stream = loadStream(streamName, obj);

    if (!auth.isAllowed(id, Authorization.Permission.Pull)) {
      throw new ForbiddenException("Cannot fetch data");
    }
    
    if(!obj.settings.storeEnabled()) {
      return Response.noContent().build();
    }
    
    ResultSet data = storage.fetchData(stream);

    logger.debug("Fetched {} records for stream {} in object {}", data.size(), streamName, obj.id);

    return Response.ok(data.toJson()).build();
  }
  
  @DELETE
  @Path("{stream}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(
          @PathParam("id") String id,
          @PathParam("stream") String streamName
  ) throws RaptorComponent.ParserException, ConfigurationException, Storage.StorageException, RaptorComponent.ValidationException, Authorization.AuthorizationException, Authentication.AuthenticationException, JsonProcessingException, RecordsetException, IOException, Indexer.IndexerException {

    ServiceObject obj = loadObject(id);
    Stream stream = loadStream(streamName, obj);

    if (!auth.isAllowed(id, Authorization.Permission.Push)) {
      throw new ForbiddenException("Cannot delete data");
    }
    
    if(!obj.settings.storeEnabled()) {
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
  ) throws RaptorComponent.ParserException, ConfigurationException, Storage.StorageException, RaptorComponent.ValidationException, Authorization.AuthorizationException, Authentication.AuthenticationException, JsonProcessingException, RecordsetException, Indexer.SearchException, IOException {

    ServiceObject obj = loadObject(id);
    Stream stream = loadStream(streamName, obj);

    if (!auth.isAllowed(id, Authorization.Permission.Pull)) {
      throw new ForbiddenException("Cannot fetch data");
    }
    
    if(!obj.settings.storeEnabled()) {
      return Response.noContent().build();
    }
    
    RecordSet data = storage.fetchLastUpdate(stream);

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
          
  ) throws RaptorComponent.ParserException, ConfigurationException, Storage.StorageException, RaptorComponent.ValidationException, Authorization.AuthorizationException, Authentication.AuthenticationException, JsonProcessingException, RecordsetException, Indexer.SearchException, Indexer.IndexerException, IOException {

    ServiceObject obj = loadObject(id);

    Stream stream = loadStream(streamName, obj);

    if (!auth.isAllowed(id, Authorization.Permission.Push)) {
      throw new ForbiddenException("Cannot push data");
    }
    
    if(obj.settings.storeEnabled()) {
      
      logger.debug("Storing data for {} on {}", obj.id, stream.name);
      
      // save data
      storage.saveData(stream, record);

      // index data (with objectId and stream props)
      try {
        indexer.indexData(stream, record);
      }
      catch(ConfigurationException | IOException | Indexer.IndexerException ex) {
        logger.error("Failed to index record for {}", obj.id);
        storage.deleteData(stream, record);
        throw ex;
      }

    }
    else {
      logger.debug("Skipped data storage for {}", obj.id);
    }
       
    emitter.trigger(EventEmitterService.EventName.push, new DataEvent(stream, record, auth.getAccessToken()));
    
    // send update on the data topic
    dispatcher.pushData(stream, record);
    
    logger.debug("Stored record for stream {} in object {}", streamName, obj.id);
    
    return Response.accepted().build();
  }
  
  
  @POST
  @Path("{stream}/search")
  @Produces(MediaType.APPLICATION_JSON)
  public Response search(
          @PathParam("id") String id,
          @PathParam("stream") String streamName,
          DataQuery query
          
  ) throws RaptorComponent.ParserException, ConfigurationException, Storage.StorageException, RaptorComponent.ValidationException, Authorization.AuthorizationException, Authentication.AuthenticationException, JsonProcessingException, RecordsetException, Indexer.SearchException, Indexer.IndexerException, IOException {

    ServiceObject obj = loadObject(id);
    Stream stream = loadStream(streamName, obj);

    if (!auth.isAllowed(id, Authorization.Permission.Pull)) {
      throw new ForbiddenException("Cannot search data");
    }
    
    if(!obj.settings.storeEnabled()) {
      return Response.noContent().build();
    }
    
    List<ResultSet> results = indexer.searchData(stream, query);
    
    logger.debug("Search data for stream {} in object {} has {} results", streamName, obj.id, results.size());
    
    return Response.ok().entity(results).build();
  }
  
  
}
