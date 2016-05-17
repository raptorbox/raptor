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
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
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
import org.createnet.raptor.http.exception.ConfigurationException;
import org.createnet.raptor.http.service.DispatcherService;
import org.createnet.raptor.models.exception.RecordsetException;
import org.createnet.raptor.models.objects.Action;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Path("/{id}/actuations")
public class ActionApi extends AbstractApi {

  final private Logger logger = LoggerFactory.getLogger(ActionApi.class);

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Action> list(
          @PathParam("id") String id
  ) throws Storage.StorageException, RaptorComponent.ParserException, ConfigurationException, Authorization.AuthorizationException, Authentication.AutenticationException, IOException {
    
    ServiceObject obj = loadObject(id);

    logger.debug("Load actions for object {}", obj.id);

    return obj.actions.values();
  }

  @GET
  @Path("{actionId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStatus(
          @PathParam("id") String id,
          @PathParam("actionId") String actionId
  ) throws RaptorComponent.ParserException, ConfigurationException, Storage.StorageException, RaptorComponent.ValidationException, Authorization.AuthorizationException, Authentication.AutenticationException, JsonProcessingException, RecordsetException {

    ServiceObject obj = loadObject(id);
    Action action = loadAction(actionId, obj);

    if (!auth.isAllowed(Authorization.Permission.Read)) {
      throw new NotAuthorizedException("Cannot fetch data");
    }

    String status = storage.getActionStatus(action);
    
    logger.debug("Fetched action {} status for object {}", action.name, obj.id);
    
    if(status == null) {
      return Response.noContent().build();
    }

    return Response.ok(status).build();
  }

  @POST
  @Path("{actionId}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.TEXT_PLAIN)
  public Response updateStatus(
          @PathParam("id") String id,
          @PathParam("actionId") String actionId,
          String body
  ) throws RaptorComponent.ParserException, ConfigurationException, Storage.StorageException, RaptorComponent.ValidationException, Authorization.AuthorizationException, Authentication.AutenticationException, JsonProcessingException, RecordsetException, IOException {

    ServiceObject obj = loadObject(id);
    Action action = loadAction(actionId, obj);

    if (!auth.isAllowed(Authorization.Permission.Execute)) {
      throw new NotAuthorizedException("Cannot fetch data");
    }
    
    storage.saveActionStatus(action, body);
    
    // notify event
    dispatcher.notifyActionEvent(DispatcherService.ActionOperation.execute, action, body);
    
    // trigger action over mqtt
    dispatcher.actionTrigger(action, body);
    
    logger.debug("Saved action {} status for object {}", action.name, obj.id);

    return Response.accepted().build();
  }

  @DELETE
  @Path("{actionId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteStatus(
          @PathParam("id") String id,
          @PathParam("actionId") String actionId
  ) throws RaptorComponent.ParserException, ConfigurationException, Storage.StorageException, RaptorComponent.ValidationException, Authorization.AuthorizationException, Authentication.AutenticationException, JsonProcessingException, RecordsetException, IOException {

    ServiceObject obj = loadObject(id);
    Action action = loadAction(actionId, obj);

    if (!auth.isAllowed(Authorization.Permission.Update)) {
      throw new NotAuthorizedException("Cannot fetch data");
    }

    storage.deleteActionStatus(action);
    dispatcher.notifyActionEvent(DispatcherService.ActionOperation.execute, action, null);
    
    logger.debug("Aborted action {} status for object {}", action.name, obj.id);

    return Response.accepted().build();
  }
  
}
