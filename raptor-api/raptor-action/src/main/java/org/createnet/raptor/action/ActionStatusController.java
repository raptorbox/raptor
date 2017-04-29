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
package org.createnet.raptor.action;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.createnet.raptor.api.common.client.ApiClientService;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.data.ActionStatus;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.response.JsonErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@RestController
@RequestMapping(value = "/")
@ApiResponses(value = {
    @ApiResponse(
            code = 200,
            message = "Ok"
    )
    ,
    @ApiResponse(
            code = 401,
            message = "Not authorized"
    )
    ,
    @ApiResponse(
            code = 403,
            message = "Forbidden"
    )
    ,
    @ApiResponse(
            code = 500,
            message = "Internal error"
    )
})
@Api(tags = {"Inventory"})
public class ActionStatusController {

    @Autowired
    private ActionStatusService actionStatusService;

    @Autowired
    private ActionStatusEventPublisher actionStatusPublisher;
    
    @Autowired
    private ApiClientService raptor;

    @RequestMapping(
            method = RequestMethod.POST, 
            value = "/{deviceId}/{actionId}"
    )
    @ApiOperation(
            value = "Trigger an action, updating the state with the request body if provided",
            notes = "The whole body is stored as a text value",
            response = ActionStatus.class,
            nickname = "trigger"
    )
    public ResponseEntity<?> trigger(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("deviceId") String deviceId,
            @PathVariable("actionId") String actionId,
            @RequestBody String status
    ) {
                
        Device device = raptor.Inventory().load(deviceId);
        
        Action action = device.getAction(actionId);
        if(action == null) {
            return JsonErrorResponse.notFound("Action not found");
        }
        
        if(status != null && !status.isEmpty()) {
            ActionStatus actionStatus = new ActionStatus(action, status);
            actionStatusService.save(actionStatus);
        }

        return ResponseEntity.accepted().build();
    }

    @RequestMapping(
            method = RequestMethod.PUT, 
            value = "/{deviceId}/{actionId}", 
            consumes = { 
                MediaType.TEXT_PLAIN_VALUE
            }
    )
    @ApiOperation(
            value = "Update the status for an action",
            notes = "The whole body is stored as a text value",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            response = ActionStatus.class,
            nickname = "setActionStatus"
    )
    public ResponseEntity<?> setActionStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("deviceId") String deviceId,
            @PathVariable("actionId") String actionId,
            @RequestBody String status
    ) {
                
        Device device = raptor.Inventory().load(deviceId);
        
        Action action = device.getAction(actionId);
        if(action == null) {
            return JsonErrorResponse.notFound("Action not found");
        }
        
        ActionStatus actionStatus = new ActionStatus(action, status);
        
        actionStatusService.save(actionStatus);
        
        return ResponseEntity.accepted().build();
    }

    @RequestMapping(
            method = RequestMethod.GET, 
            value = "/{deviceId}/{actionId}", 
            produces = { 
                MediaType.APPLICATION_JSON_VALUE, 
                MediaType.APPLICATION_JSON_UTF8_VALUE 
            }
    )
    @ApiOperation(
            value = "Return the status information for an action",
            notes = "",
            response = ActionStatus.class,
            nickname = "getActionStatus"
    )
    public ResponseEntity<?> getActionStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("deviceId") String deviceId,
            @PathVariable("actionId") String actionId
    ) {
                
        Device device = raptor.Inventory().load(deviceId);
        
        Action action = device.getAction(actionId);
        if(action == null) {
            return JsonErrorResponse.notFound("Action not found");
        }
        
        ActionStatus status = actionStatusService.get(deviceId, action.name);
        
        return ResponseEntity.ok(status);
    }

    @RequestMapping(
            method = RequestMethod.GET, 
            value = "/{deviceId}/{actionId}", 
            produces = { 
                MediaType.TEXT_PLAIN_VALUE
            }
    )
    @ApiOperation(
            value = "Return the textual status value of an action",
            notes = "",
            response = String.class,
            nickname = "getActionStatusValue"
    )
    public ResponseEntity<?> getActionStatusValue(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("deviceId") String deviceId,
            @PathVariable("actionId") String actionId
    ) {
                
        Device device = raptor.Inventory().load(deviceId);
        
        Action action = device.getAction(actionId);
        if(action == null) {
            return JsonErrorResponse.notFound("Action not found");
        }
        
        ActionStatus status = actionStatusService.get(deviceId, action.name);
        
        return ResponseEntity.ok(status);
    }

}
