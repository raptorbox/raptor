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
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.http.service.StorageService;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.http.service.AuthService;
import org.createnet.raptor.http.service.DispatcherService;
import org.createnet.raptor.http.service.IndexerService;
import org.createnet.search.raptor.search.Indexer;
import org.createnet.raptor.http.exception.ConfigurationException;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.exception.RecordsetException;
import org.createnet.raptor.models.objects.Stream;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Path("/{id}/streams")
public class DataService {

  final private Logger logger = LoggerFactory.getLogger(DataService.class);

  @Inject
  StorageService storage;

  @Inject
  IndexerService indexer;

  @Inject
  DispatcherService dispatcher;

  @Inject
  AuthService auth;
  
  protected ServiceObject loadObject(String id) throws Authorization.AuthorizationException, Storage.StorageException, RaptorComponent.ParserException, ConfigurationException {

    if (!auth.isAllowed(id, Authorization.Permission.Read)) {
      throw new NotAllowedException("Cannot access object");
    }

    return storage.getObject(id);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Stream> list(@PathParam("id") String id)
          throws Storage.StorageException, RaptorComponent.ParserException, ConfigurationException, Authorization.AuthorizationException, Authentication.AutenticationException, IOException {

    ServiceObject obj = loadObject(id);

    logger.debug("Load streams for object {}", obj.id);

    return obj.streams.values();
  }

  @GET
  @Path("{stream}")
  @Produces(MediaType.APPLICATION_JSON)
  public ResultSet fetch(
          @PathParam("id") String id,
          @PathParam("stream") String streamName
  ) throws RaptorComponent.ParserException, ConfigurationException, Storage.StorageException, RaptorComponent.ValidationException, Authorization.AuthorizationException, Authentication.AutenticationException, JsonProcessingException, RecordsetException {

    ServiceObject obj = loadObject(id);

    Stream stream = obj.streams.get(streamName);

    if (stream == null) {
      throw new NotFoundException("Stream " + streamName + " not found");
    }

    if (!auth.isAllowed(Authorization.Permission.Pull)) {
      throw new NotAuthorizedException("Cannot fetch data");
    }

    ResultSet data = storage.fetchData(stream);

    logger.debug("Fetched {} records for stream {} in object {}", data.size(), streamName, obj.id);

    return data;
  }

  @GET
  @Path("{stream}/lastUpdate")
  @Produces(MediaType.APPLICATION_JSON)
  public Response fetchLastUpdate(
          @PathParam("id") String id,
          @PathParam("stream") String streamName
  ) throws RaptorComponent.ParserException, ConfigurationException, Storage.StorageException, RaptorComponent.ValidationException, Authorization.AuthorizationException, Authentication.AutenticationException, JsonProcessingException, RecordsetException, Indexer.SearchException, IOException {

    ServiceObject obj = loadObject(id);

    Stream stream = obj.streams.get(streamName);

    if (stream == null) {
      throw new NotFoundException("Stream " + streamName + " not found");
    }

    if (!auth.isAllowed(Authorization.Permission.Pull)) {
      throw new NotAuthorizedException("Cannot fetch data");
    }

    RecordSet data = indexer.searchLastUpdate(stream);

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
          
  ) throws RaptorComponent.ParserException, ConfigurationException, Storage.StorageException, RaptorComponent.ValidationException, Authorization.AuthorizationException, Authentication.AutenticationException, JsonProcessingException, RecordsetException, Indexer.SearchException, Indexer.IndexerException, IOException {

    ServiceObject obj = loadObject(id);

    Stream stream = obj.streams.get(streamName);

    if (stream == null) {
      throw new NotFoundException("Stream " + streamName + " not found");
    }

    if (!auth.isAllowed(Authorization.Permission.Push)) {
      throw new NotAuthorizedException("Cannot push data");
    }
    
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
    
    // notify onData & data topic
    dispatcher.notifyDataEvent(stream, record);
    
    logger.debug("Stored record for stream {} in object {}", streamName, obj.id);
    
    return Response.ok().build();
  }
  
  
}
