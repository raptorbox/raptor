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
import org.createnet.raptor.models.objects.ServiceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.createnet.raptor.http.events.ActionEvent;
import org.createnet.raptor.http.service.EventEmitterService;
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
    ) {

        ServiceObject obj = loadObject(id);
        Action action = loadAction(actionId, obj);

        if (!auth.isAllowed(obj, Authorization.Permission.Read)) {
            throw new ForbiddenException("Cannot fetch data");
        }

        ActionStatus actionStatus = storage.getActionStatus(action);

        logger.debug("Fetched action {} status for object {}", action.name, obj.id);

        if (actionStatus == null) {
            return Response.noContent().build();
        }

        return Response.ok(actionStatus.toString()).build();
    }

    @POST
    @Path("{actionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    public String updateStatus(
            @PathParam("id") String id,
            @PathParam("actionId") String actionId,
            String body
    ) {

        ServiceObject obj = loadObject(id);
        Action action = loadAction(actionId, obj);

        if (!auth.isAllowed(obj, Authorization.Permission.Execute)) {
            throw new ForbiddenException("Cannot fetch data");
        }

        ActionStatus actionStatus = storage.saveActionStatus(action, body);
        emitter.trigger(EventEmitterService.EventName.execute, new ActionEvent(action, actionStatus));

        logger.debug("Saved action {} status for object {}", action.name, obj.id);

        return actionStatus.toString();
    }

    @DELETE
    @Path("{actionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteStatus(
            @PathParam("id") String id,
            @PathParam("actionId") String actionId
    ) {

        ServiceObject obj = loadObject(id);
        Action action = loadAction(actionId, obj);

        if (!auth.isAllowed(obj, Authorization.Permission.Update)) {
            throw new ForbiddenException("Cannot fetch data");
        }

        storage.deleteActionStatus(action);

        emitter.trigger(EventEmitterService.EventName.deleteAction, new ActionEvent(action, null));

        logger.debug("Aborted action {} status for object {}", action.name, obj.id);

        return Response.accepted().build();
    }

}
