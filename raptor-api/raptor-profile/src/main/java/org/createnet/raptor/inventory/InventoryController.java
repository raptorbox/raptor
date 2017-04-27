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
package org.createnet.raptor.inventory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.response.JsonErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@RequestMapping(value = "/inventory")
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
@Api(tags = { "Inventory" })
public class InventoryController {
    
    @Autowired
    private DeviceService deviceService;

    @RequestMapping(method = RequestMethod.GET, value = "/")
    @ApiOperation(
            value = "Return the user devices",
            notes = "",
            response = Device.class,
            nickname = "getDevices"
    )
    public ResponseEntity<?> getDevices(
            @AuthenticationPrincipal User currentUser
    ) {
        List<Device> devices = deviceService.list(currentUser.getUuid());
        return ResponseEntity.ok(devices);
    }


    @RequestMapping(method = RequestMethod.POST, value = "/")
    @ApiOperation(
            value = "Create a device instance",
            notes = "",
            response = Device.class,
            nickname = "createDevice"
    )
    public ResponseEntity<?> createDevice(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Device device
    ) {

        try {
            device.validate();
        }
        catch(RaptorComponent.ValidationException ex) {
            return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST, "Device definition invalid: " + ex.getMessage());
        }

        deviceService.save(device);

        return ResponseEntity.ok(device.toJSON());
    }    
    
    @RequestMapping(method = RequestMethod.GET, value = "/{deviceId}")
    @ApiOperation(
            value = "Return a device instance definition",
            notes = "",
            response = Device.class,
            nickname = "getDevice"
    )
    public ResponseEntity<?> getDevice(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("deviceId") String deviceId
    ) {
        Device device = deviceService.get(deviceId);
        if (device == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "Not found");
        }
        return ResponseEntity.ok(device.toJSON());
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{deviceId}")
    @ApiOperation(
            value = "Update a device instance",
            notes = "",
            response = Device.class,
            nickname = "updateDevice"
    )
    public ResponseEntity<?> updateDevice(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("deviceId") String deviceId,
            @RequestBody Device body
    ) {

        Device device = deviceService.get(deviceId);

        if (device == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "Device not found");
        }

        deviceService.save(device);

        return ResponseEntity.ok(device.toJSON());
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{deviceId}")
    @ApiOperation(
            value = "Delete a device instance",
            notes = "",
            response = Device.class,
            nickname = "deleteDevice"
    )
    public ResponseEntity<?> deleteDevice(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("deviceId") String deviceId
    ) {

        Device device = deviceService.get(deviceId);
        if (device == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "Device not found");
        }

        deviceService.delete(device);

        return ResponseEntity.accepted().build();
    }

}
