/*
 * Copyright 2017 FBK/CREATE-NET
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
import org.createnet.raptor.models.data.ActionStatus;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.Stream;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Path("/{id}/actions")
@Produces(MediaType.APPLICATION_JSON)
@ApiResponses(value = {
    @ApiResponse(
            code = 200, 
            message = "Ok"
    ),
    @ApiResponse(
            code = 401,
            message = "Not authorized"
    ),
    @ApiResponse(
            code = 403, 
            message = "Forbidden"
    ),
    @ApiResponse(
            code = 500, 
            message = "Internal error"
    )
})
@Api(tags = { "Action" })
public class ActionApi extends AbstractApi {

    final private Logger logger = LoggerFactory.getLogger(ActionApi.class);

    @GET
    @ApiOperation(
            value = "Return the list of actions definition",
            notes = "",
            response = Action.class,
            responseContainer = "Collection",
            nickname = "getActionDefinition"
    )
    @ApiResponses(value = {})
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
    @ApiOperation(
            value = "Return the current state of an action",
            notes = "",
            response = ActionStatus.class,
            nickname = "getActionStatus"
    )
    @ApiResponses(value = {})
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
    @Consumes(MediaType.TEXT_PLAIN)    
    @ApiOperation(
            value = "Set the state of an action",
            notes = "",
            response = ActionStatus.class,
            nickname = "setActionStatus"
    )
    @ApiResponses(value = {})
    public Response setStatus(
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
        return Response.ok(actionStatus).build();
    }

    @DELETE
    @Path("{actionId}")
    @ApiOperation(
            value = "Set the state of an action",
            notes = "",
            response = ActionStatus.class,
            nickname = "deleteActionStatus"
    )
    @ApiResponses(value = {
        @ApiResponse(
                code = 202, 
                message = "Accepted"
        )
    })
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
