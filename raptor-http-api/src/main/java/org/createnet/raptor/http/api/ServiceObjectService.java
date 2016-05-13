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


import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
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
import org.createnet.raptor.models.objects.serializer.ServiceObjectView;
import org.createnet.search.raptor.search.query.impl.es.ObjectQuery;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Path("/")
public class ServiceObjectService {

  final private Logger logger = LoggerFactory.getLogger(ServiceObjectService.class);

  @Inject
  StorageService storage;
  
  @Inject
  IndexerService indexer;
  
  @Inject
  DispatcherService dispatcher;
  
  @Inject
  AuthService auth;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> list() throws Storage.StorageException, RaptorComponent.ParserException, ConfigurationException, Authorization.AuthorizationException, Authentication.AutenticationException, IOException {

    if (!auth.isAllowed(Authorization.Permission.Read)) {
      throw new NotAllowedException("Cannot list objects");
    }

    List<ServiceObject> list = storage.listObjects();
    List<String> idList = new ArrayList();
    for (ServiceObject obj : list) {
      idList.add(obj.id);
    }
    
    return idList;
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response create(ServiceObject obj) throws RaptorComponent.ParserException, ConfigurationException, Storage.StorageException, RaptorComponent.ValidationException, Authorization.AuthorizationException, Authentication.AutenticationException {

    if (!auth.isAllowed(Authorization.Permission.Create)) {
      throw new NotAuthorizedException("Cannot create object");
    }

    storage.saveObject(obj);


    try {
      indexer.indexObject(obj, true);
    } catch (Indexer.IndexerException ex) {
      
      logger.error("Indexing error occured", ex);
      logger.warn("Removing object {} from storage", obj.id);
      
      storage.deleteObject(obj.id);
      
      throw new InternalServerErrorException();
    }    
    
    dispatcher.notifyObjectEvent(DispatcherService.ObjectOperation.create, obj);
    
    logger.debug("Created new object {} for {}", obj.id, auth.getUser().getUserId());
    
    return Response.created(URI.create("/" + obj.id)).entity(obj.toJSON(ServiceObjectView.IdOnly)).build();
  }

  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public ServiceObject update(ServiceObject obj) throws RaptorComponent.ParserException, ConfigurationException, Storage.StorageException, RaptorComponent.ValidationException, Authorization.AuthorizationException, Authentication.AutenticationException, Indexer.IndexerException {

    ServiceObject storedObj = storage.getObject(obj.id);

    if (storedObj == null) {
      throw new NotFoundException();
    }

    if(!auth.isAllowed(obj.id, Authorization.Permission.Update)) {
      throw new NotAuthorizedException("Cannot update object");
    }
    
    if (!storedObj.userId.equals(auth.getUser().getUserId())) {
      logger.warn("User {} tried to update object {} owned by {}", auth.getUser().getUserId(), storedObj.id, storedObj.userId);
      throw new NotFoundException();
    }

    logger.debug("Updating object {}", obj.id);
    storage.saveObject(obj);
    indexer.indexObject(obj, false);
    
    dispatcher.notifyObjectEvent(DispatcherService.ObjectOperation.update, obj);
    
    logger.debug("Updated object {} for {}", obj.id, auth.getUser().getUserId());
    return obj;
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public ServiceObject load(@PathParam("id") String id) throws Storage.StorageException, RaptorComponent.ParserException, ConfigurationException, Authorization.AuthorizationException {

    logger.debug("Load object {}", id);

    ServiceObject obj = storage.getObject(id);

    if (obj == null) {
      logger.debug("Object {} not found", id);
      throw new NotFoundException();
    }

    if(!auth.isAllowed(obj.id, Authorization.Permission.Read)) {
      throw new NotAuthorizedException("Cannot read object");
    }
    
    return obj;
  }

  @DELETE
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(@PathParam("id") String id) throws Storage.StorageException, RaptorComponent.ParserException, Authorization.AuthorizationException, ConfigurationException, Indexer.IndexerException, Authentication.AutenticationException  {

    logger.debug("delete object {}", id);

    ServiceObject obj = storage.getObject(id);

    if (obj == null) {
      logger.debug("Object {} not found", id);
      throw new NotFoundException();
    }

    if(!auth.isAllowed(obj.id, Authorization.Permission.Delete)) {
      throw new NotAuthorizedException("Cannot delete object");
    }

    storage.deleteObject(id);
    indexer.deleteObject(id);
    
    dispatcher.notifyObjectEvent(DispatcherService.ObjectOperation.delete, obj);
    
    return Response.status(Response.Status.OK).build();
  }

  @POST
  @Path("/search")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public List<ServiceObject> search(ObjectQuery query) throws Storage.StorageException, RaptorComponent.ParserException, ConfigurationException, Authorization.AuthorizationException, Authentication.AutenticationException, Indexer.SearchException, IOException {

    if (!auth.isAllowed(Authorization.Permission.Read)) {
      throw new NotAllowedException("Cannot search for objects");
    }
    
    List<ServiceObject> list = indexer.searchObject(query);
    return list;
  }  
  
}
