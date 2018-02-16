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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.createnet.raptor.common.client.ApiClientService;
import org.createnet.raptor.common.client.InternalApiClientService;
import org.createnet.raptor.common.query.DeviceQueryBuilder;
import org.createnet.raptor.models.app.App;
import org.createnet.raptor.models.app.AppUser;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.models.query.DeviceQuery;
import org.createnet.raptor.models.response.JsonErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.querydsl.core.types.Predicate;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@RestController
@RequestMapping(value = "/inventory")
@ApiResponses(value = {
    @ApiResponse(code = 200, message = "Ok")
    , @ApiResponse(code = 401, message = "Not authorized")
    , @ApiResponse(code = 403, message = "Forbidden")
    , @ApiResponse(code = 500, message = "Internal error")})
@Api(tags = {"Inventory"})
public class InventoryController {

    protected final Logger log = LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceEventPublisher eventPublisher;

    @Autowired
    private ApiClientService raptor;
    
    @Autowired
    InternalApiClientService api;


    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Return the user devices", notes = "", response = Device.class, nickname = "getDevices")
    @PreAuthorize("@raptorSecurity.list(principal, 'device')")
    public ResponseEntity<?> getDevices(
            @AuthenticationPrincipal User currentUser,
            Pageable pageable,
            HttpServletRequest request
    ) {
        String userId = currentUser.getId();
        if (currentUser.isAdmin()) {
            userId = null;
        }
        
        Map<String,	String> queryParams = new HashMap<>();
        
        String queryString = request.getQueryString();
        if(userId != null) {
        	queryParams.put("userId", userId);
    	}
        String[] params = queryString.split("&");
        for(String p: params) {
        	String[] q = p.split("=");
        	queryParams.put(q[0], q[1]);
        }

        DeviceQuery query = DeviceQuery.query(queryParams);
        DeviceQueryBuilder qb = new DeviceQueryBuilder(query);
        Predicate predicate = qb.getORPredicate();
 
        Page<Device> result = deviceService.search(predicate, pageable);
        
        return ResponseEntity.ok(result);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = "Create a device instance", notes = "", response = Device.class, nickname = "createDevice")
    @PreAuthorize("@raptorSecurity.can(principal, 'device', 'create', #device)")
    public ResponseEntity<?> createDevice(@AuthenticationPrincipal User currentUser, @RequestBody Device device) {

        device.setDefaults();

        try {
            device.validate();
        } catch (RaptorComponent.ValidationException ex) {
            return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST,
                    "Device definition is not valid: " + ex.getMessage());
        }

        String devUserId = device.userId();
        // set current user if empty
        if (devUserId == null || devUserId.isEmpty()) {
            devUserId = currentUser.getId();
        }

        // set ownership to current user
        device.userId(currentUser.getId());

        // super_admin can set ownership
        if (currentUser.isAdmin()) {
            // set ownership as per request, fallback to current user if empty
            device.userId(devUserId);
        } else {
            // std user is the owner
            device.userId(currentUser.getId());
        }

        deviceService.save(device);

        eventPublisher.create(device);

        log.info("Created device {} for user {}", device.id(), device.userId());

        return ResponseEntity.ok(device.toJSON());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{deviceId}")
    @ApiOperation(value = "Return a device instance definition", notes = "", response = Device.class, nickname = "getDevice")
    @PreAuthorize("@raptorSecurity.can(principal, 'device', 'read', #deviceId)")
    public ResponseEntity<?> getDevice(@AuthenticationPrincipal User currentUser,
            @PathVariable("deviceId") String deviceId) {

        Device device = deviceService.get(deviceId);
        if (device == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "Not found");
        }

        return ResponseEntity.ok(device);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{deviceId}")
    @ApiOperation(value = "Update a device instance", notes = "", response = Device.class, nickname = "updateDevice")
    @PreAuthorize("@raptorSecurity.can(principal, 'device', 'update', #deviceId)")
    public ResponseEntity<?> updateDevice(@AuthenticationPrincipal User currentUser,
            @PathVariable("deviceId") String deviceId, @RequestBody Device body) {

        Device device = deviceService.get(deviceId);

        if (device == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "Device not found");
        }

        for (Iterator<Entry<String, Stream>> iterator = device.streams().entrySet().iterator(); iterator.hasNext();) {
            Entry<String, Stream> entry = (Entry<String, Stream>) iterator.next();
            String key = entry.getKey();
            if (!body.streams().containsKey(key)) {
                Stream stream = device.stream(key);
                if (stream == null) {
                    return JsonErrorResponse.notFound("Stream not found");
                }
                stream.setDevice(device);
                raptor.Stream().delete(stream);
                iterator.remove();
            }
        }

        device.merge(body);

        // A std user can NOT change ownership even if has `update` permission
        // Admin users can change ownership
        if (currentUser.isAdmin()) {

            // set ownership as per request if not empty
            if (body.userId() != null && !body.userId().isEmpty()) {
                device.userId(body.userId());
            }

        }

        // ensure a default is always set (cover legacy cases where userId may be null)
        if (device.userId() == null && device.userId().isEmpty()) {
            device.userId(currentUser.getId());
        }

        device.validate();

        deviceService.save(device);

        eventPublisher.update(device);

        log.info("Updated device {} for user {}", device.id(), device.userId());
        return ResponseEntity.ok(device.toJSON());
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{deviceId}")
    @ApiOperation(value = "Delete a device instance", notes = "", response = Device.class, nickname = "deleteDevice")
    @PreAuthorize("@raptorSecurity.can(principal, 'device', 'delete', #deviceId)")
    public ResponseEntity<?> deleteDevice(@AuthenticationPrincipal User currentUser,
            @PathVariable("deviceId") String deviceId) {

        Device device = deviceService.get(deviceId);
        if (device == null) {
            return JsonErrorResponse.entity(HttpStatus.NOT_FOUND, "Device not found");
        }

        deviceService.delete(device);

        eventPublisher.delete(device);

        return ResponseEntity.accepted().build();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/search")
    @ApiOperation(value = "Search for device instances", notes = "", response = Device.class, nickname = "searchDevices")
    @PreAuthorize("@raptorSecurity.can(principal, 'device', 'read')")
    public ResponseEntity<?> searchDevices(
            @AuthenticationPrincipal User currentUser,
            @RequestParam MultiValueMap<String, String> parameters, 
            @RequestBody DeviceQuery query
    ) {

        if (query.isEmpty()) {
            return JsonErrorResponse.badRequest();
        }
        
        if (!currentUser.isAdmin()) {
            query.userId(currentUser.getId());
        }
        
        if(query.domain.getEquals() != null) {
        	App app = api.App().load(query.domain.getEquals());
        	AppUser user = app.getUsers().stream().filter(d -> d.getId().equals(currentUser.getId())).findFirst().get();
        	if(user != null) {
        		List<String> roles = user.getRoles();
        		if(roles.stream().filter(r -> r.equals("admin_user") || r.equals("admin") || r.equals("admin_app") || r.equals("read_user")).findFirst() != null) {
        			query.userId(null);
        		}
        	}
        }

        DeviceQueryBuilder qb = new DeviceQueryBuilder(query);
        Predicate predicate = qb.getPredicate();
        Pageable paging = qb.getPaging();

        Page<Device> pagedList = deviceService.search(predicate, paging);

        return ResponseEntity.ok(pagedList);
    }

}
