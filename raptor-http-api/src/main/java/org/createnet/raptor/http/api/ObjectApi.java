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
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
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
import org.createnet.raptor.search.raptor.search.Indexer;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.createnet.raptor.http.events.ObjectEvent;
import org.createnet.raptor.http.service.EventEmitterService;
import org.createnet.raptor.models.exception.RecordsetException;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.Channel;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.search.raptor.search.query.impl.es.ObjectQuery;

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
  @ApiOperation(value = "List available devices definition", notes = "")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Ok"),
    @ApiResponse(code = 403, message = "Forbidden")
  })
  public List<String> list() throws Storage.StorageException, RaptorComponent.ParserException, ConfigurationException, Authorization.AuthorizationException, Authentication.AuthenticationException, Indexer.IndexerException {

    if (!auth.isAllowed(Authorization.Permission.List)) {
      throw new ForbiddenException("Cannot list objects");
    }
    
    // TODO: List device which access is allowed
    
    List<ServiceObject> list = indexer.getObjects(auth.getUser().getUserId());
    List<String> idList = new ArrayList();
    list.stream().forEach((obj) -> {
      idList.add(obj.id);
    });

    return idList;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Create a new device definition", notes = "")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Ok"),
    @ApiResponse(code = 403, message = "Forbidden"),
    @ApiResponse(code = 500, message = "Internal error")
  })
  public Response create(ServiceObject obj) throws RaptorComponent.ParserException, ConfigurationException, Storage.StorageException, RaptorComponent.ValidationException, Authorization.AuthorizationException, Authentication.AuthenticationException, IOException, Indexer.IndexerException, RecordsetException {

    if (!auth.isAllowed(Authorization.Permission.Create)) {
      throw new ForbiddenException("Cannot create object");
    }

    obj.id = null;
    obj.userId = auth.getUser().getUserId();

    storage.saveObject(obj);

    try {
      indexer.indexObject(obj, true);
    } catch (Indexer.IndexerException ex) {

      logger.error("Indexing error occured", ex);
      logger.warn("Removing object {} from storage", obj.id);

      storage.deleteObject(obj);

      throw new InternalServerErrorException("Failed to index device");
    }

    try {

      auth.sync(auth.getAccessToken(), obj, Authentication.SyncOperation.CREATE);

    } catch (Authentication.AuthenticationException | ConfigurationException ex) {

      logger.error("Error syncing object to auth system: {}", ex.getMessage());
      logger.error("Auth sync failed, aborting creation of object {}", obj.id);

      storage.deleteObject(obj);
      indexer.deleteObject(obj);

      throw new InternalServerErrorException("Failed to sync device");
    }

    emitter.trigger(EventEmitterService.EventName.create, new ObjectEvent(obj, auth.getAccessToken()));

    logger.debug("Created new object {} for {}", obj.id, auth.getUser().getUserId());

    return Response.created(URI.create("/" + obj.id)).entity(obj.toJSON()).build();
  }

  @PUT
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Update a device definition", notes = "")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Ok"),
    @ApiResponse(code = 404, message = "Not Found"),
    @ApiResponse(code = 403, message = "Forbidden")
  })
  public String update(@PathParam("id") String id, ServiceObject obj) throws RaptorComponent.ParserException, ConfigurationException, Storage.StorageException, RaptorComponent.ValidationException, Authorization.AuthorizationException, Authentication.AuthenticationException, Indexer.IndexerException, IOException, RecordsetException {

    ServiceObject storedObj = loadObject(id);

    if (obj.id == null || obj.id.isEmpty()) {
      obj.id = storedObj.id;
    }

    if (!storedObj.id.equals(obj.id)) {
      throw new NotFoundException("Request id does not match payload defined id");
    }

    if (!auth.isAllowed(obj, Authorization.Permission.Update)) {
      throw new ForbiddenException("Cannot update object");
    }

    if (!storedObj.userId.equals(auth.getUser().getUserId())) {
      logger.warn("User {} tried to update object {} owned by {}", auth.getUser().getUserId(), storedObj.id, storedObj.userId);
      throw new NotFoundException();
    }

    logger.debug("Updating object {}", obj.id);

    // @TODO: handle proper object update, ensuring stream data integrity
    storedObj.customFields.clear();
    storedObj.customFields.putAll(obj.customFields);

    // update settings
    storedObj.settings.storeData = obj.settings.storeData;
    storedObj.settings.eventsEnabled = obj.settings.eventsEnabled;

    // merge stream definitions
    List<Stream> changedStreams = getChangedStreams(storedObj, obj);

    storedObj.streams.clear();
    storedObj.addStreams(obj.streams.values());

    // merge action definitions
    List<Action> changedActions = getChangedActions(storedObj, obj);

    storedObj.actions.clear();
    storedObj.addActions(obj.actions.values());

    storage.saveObject(storedObj);
    indexer.indexObject(storedObj, false);

    // clean up data for changed stream and actions
    storage.deleteData(changedStreams);
    indexer.deleteData(changedStreams);
    storage.deleteActionStatus(changedActions);

    emitter.trigger(EventEmitterService.EventName.update, new ObjectEvent(storedObj, auth.getAccessToken()));

    logger.debug("Updated object {} for {}", storedObj.id, auth.getUser().getUserId());

    return obj.toJSON();
  }

  @GET
  @Path("{id}")
  @ApiOperation(value = "Return a device definition", notes = "")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Ok"),
    @ApiResponse(code = 403, message = "Forbidden")
  })
  public String load(@PathParam("id") String id) throws Storage.StorageException, RaptorComponent.ParserException, ConfigurationException, Authorization.AuthorizationException, Indexer.IndexerException, Authentication.AuthenticationException {

    logger.debug("Load object {}", id);

    ServiceObject obj = loadObject(id);

    if (!auth.isAllowed(obj, Authorization.Permission.Read)) {
      throw new ForbiddenException("Cannot read object");
    }

    return obj.toJSON();
  }

  @DELETE
  @Path("{id}")
  @ApiOperation(value = "Delete a device definition", notes = "")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Ok"),
    @ApiResponse(code = 403, message = "Forbidden"),
    @ApiResponse(code = 500, message = "Internal error")
  })  
  public Response delete(@PathParam("id") String id) throws Storage.StorageException, RaptorComponent.ParserException, Authorization.AuthorizationException, ConfigurationException, Indexer.IndexerException, Authentication.AuthenticationException, IOException, RecordsetException {

    ServiceObject obj = loadObject(id);

    if (!auth.isAllowed(obj, Authorization.Permission.Delete)) {
      throw new ForbiddenException("Cannot delete object");
    }

    try {
      auth.sync(auth.getAccessToken(), obj, Authentication.SyncOperation.DELETE);
    } catch (Authentication.AuthenticationException | ConfigurationException ex) {
      logger.error("Auth sync failed, aborting deletion of object {}", obj.id);
      throw new InternalServerErrorException("Failed to sync device");
    }

    storage.deleteObject(obj);
    indexer.deleteObject(obj);

    emitter.trigger(EventEmitterService.EventName.delete, new ObjectEvent(obj, auth.getAccessToken()));

    logger.debug("Deleted object {}", id);

    return Response.status(Response.Status.OK).build();
  }

  @POST
  @Path("/search")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Search for object definitions", notes = "")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Ok"),
    @ApiResponse(code = 403, message = "Forbidden")
  })  
  public List<String> search(ObjectQuery query) throws Storage.StorageException, RaptorComponent.ParserException, ConfigurationException, Authorization.AuthorizationException, Authentication.AuthenticationException, Indexer.SearchException, IOException, Indexer.IndexerException {

    if (!auth.isAllowed(Authorization.Permission.List)) {
      throw new ForbiddenException("Cannot search for objects");
    }

    query.setUserId(auth.getUser().getUserId());

    List<ServiceObject> list = indexer.searchObject(query);

    List<String> results = new ArrayList();
    for (ServiceObject serviceObject : list) {
      results.add(serviceObject.getId());
    }

    return results;
  }

  private List<Stream> getChangedStreams(ServiceObject storedObj, ServiceObject obj) {

    List<Stream> changedStream = new ArrayList();

    // loop previous stream list and find missing streams
    for (Map.Entry<String, Stream> item : storedObj.streams.entrySet()) {

      String streamName = item.getKey();
      Stream stream = item.getValue();

      // stream found
      if (obj.streams.containsKey(streamName)) {

        // loop stream and find changed channels
        for (Map.Entry<String, Channel> channelItem : stream.channels.entrySet()) {

          String channelName = channelItem.getKey();
          Channel channel = channelItem.getValue();

          if (storedObj.streams.get(streamName).channels.containsKey(channelName)) {
            // check if channel definition changed
            if (!storedObj.streams.get(streamName).channels.get(channelName).type.equals(channel.type)) {
              changedStream.add(stream);
              break;
            }
          } else {
            // channel has gone, drop stream
            changedStream.add(stream);
            break;
          }

        }
      } else {
        // drop stream
        changedStream.add(stream);
        storedObj.streams.remove(streamName);
      }

    }

    return changedStream;
  }

  private List<Action> getChangedActions(ServiceObject storedObj, ServiceObject obj) {
    List<Action> changedAction = new ArrayList();
    obj.actions.values().stream().filter((action) -> (!storedObj.actions.containsKey(action.name))).forEachOrdered((action) -> {
        changedAction.add(action);
      });
    return changedAction;
  }

}
