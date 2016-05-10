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
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.http.service.StorageService;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.http.service.AuthService;

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
  AuthService auth;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<ServiceObject> listObjects() throws Storage.StorageException, RaptorComponent.ParserException, IOException, Authorization.AuthorizationException {

    if (!auth.isAllowed(Authorization.Permission.Read)) {
      throw new NotAllowedException("Cannot list objects");
    }

    List<ServiceObject> list = storage.listObjects(auth.getUserId());
    return list;
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createObject(ServiceObject obj) throws RaptorComponent.ParserException, IOException, Storage.StorageException, RaptorComponent.ValidationException, Authorization.AuthorizationException {

    if (!auth.isAllowed(Authorization.Permission.Create)) {
      throw new NotAllowedException("Cannot create object");
    }

    storage.saveObject(obj);

    logger.debug("Created new object {} for {}", obj.id, auth.getUserId());
    return Response.ok("{ \"id\": \"" + obj.id + "\" }").build();
  }

  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateObject(ServiceObject obj) throws RaptorComponent.ParserException, IOException, Storage.StorageException, RaptorComponent.ValidationException, Authorization.AuthorizationException {

    ServiceObject storedObj = storage.getObject(obj.id);

    if (storedObj == null) {
      throw new NotFoundException();
    }

    if(!auth.isAllowed(obj.id, Authorization.Permission.Update)) {
      throw new NotAllowedException("Cannot update object");
    }
    
    if (!storedObj.userId.equals(obj.userId)) {
      logger.warn("User {} tried to update object {} owned by {}", auth.getUserId(), storedObj.id, storedObj.userId);
      throw new NotFoundException();
    }
    
    storage.saveObject(obj);

    logger.debug("Updated object {} for {}", obj.id, auth.getUserId());
    return Response.ok("{ \"id\": \"" + obj.id + "\" }").build();
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public ServiceObject loadObject(@PathParam("id") String id) throws Storage.StorageException, RaptorComponent.ParserException, IOException, Authorization.AuthorizationException {

    logger.debug("Load object {}", id);

    ServiceObject obj = storage.getObject(id);

    if (obj == null) {
      logger.debug("Object {} not found", id);
      throw new NotFoundException();
    }

    if(!auth.isAllowed(obj.id, Authorization.Permission.Read)) {
      throw new NotAllowedException("Cannot update object");
    }    
    
    return obj;
  }

}
