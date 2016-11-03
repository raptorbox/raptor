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
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.events.Event.EventName;
import org.createnet.raptor.models.objects.ServiceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.createnet.raptor.events.type.ActionEvent;
import org.createnet.raptor.service.tools.EventEmitterService;
import org.createnet.raptor.models.data.ActionStatus;
import org.createnet.raptor.models.objects.Action;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Path("/{id}/actions")
public class ActionApi extends AbstractApi {

    final private Logger logger = LoggerFactory.getLogger(ActionApi.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Action> list(
            @PathParam("id") String id
    ) {

        ServiceObject obj = objectManager.load(id);

        if (!auth.isAllowed(obj, Authorization.Permission.Read)) {
            throw new ForbiddenException("Cannot load object");
        }

        logger.debug("Load actions for object {}", obj.id);
        return obj.actions.values();
    }

    @GET
    @Path("{actionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus(
            @PathParam("id") String objectId,
            @PathParam("actionId") String actionId
    ) {

        Action action = actionManager.load(objectId, actionId);
        ServiceObject obj = action.getServiceObject();

        if (!auth.isAllowed(obj, Authorization.Permission.Execute)) {
            throw new ForbiddenException("Cannot access action status");
        }

        ActionStatus actionStatus = actionManager.getStatus(action);
        if (actionStatus == null) {
            return Response.noContent().build();
        }

        return Response.ok(actionStatus).build();
    }

    @POST
    @Path("{actionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    public String setStatus(
            @PathParam("id") String objectId,
            @PathParam("actionId") String actionId,
            String body
    ) {

        
        Action action = actionManager.load(objectId, actionId);
        ServiceObject obj = action.getServiceObject();

        if (!auth.isAllowed(obj, Authorization.Permission.Execute)) {
            throw new ForbiddenException("Cannot modify action status");
        }
        
        ActionStatus actionStatus = actionManager.setStatus(action, body);

        return actionStatus.toString();
    }

    @DELETE
    @Path("{actionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteStatus(
            @PathParam("id") String objectId,
            @PathParam("actionId") String actionId
    ) {

        Action action = actionManager.load(objectId, actionId);
        ServiceObject obj = action.getServiceObject();
        
        if (!auth.isAllowed(obj, Authorization.Permission.Execute)) {
            throw new ForbiddenException("Cannot modify action status");
        }
        
        actionManager.removeStatus(action);

        return Response.accepted().build();
    }

}
