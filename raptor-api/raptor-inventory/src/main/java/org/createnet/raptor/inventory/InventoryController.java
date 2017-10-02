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

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.createnet.raptor.common.client.ApiClientService;
import org.createnet.raptor.common.query.DeviceQueryBuilder;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.models.query.DeviceQuery;
import org.createnet.raptor.models.response.JsonErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
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
@ApiResponses(value = { @ApiResponse(code = 200, message = "Ok"), @ApiResponse(code = 401, message = "Not authorized"),
		@ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 500, message = "Internal error") })
@Api(tags = { "Inventory" })
public class InventoryController {

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private DeviceEventPublisher eventPublisher;

	@Autowired
	private ApiClientService raptor;

	@RequestMapping(method = RequestMethod.GET)
	@ApiOperation(value = "Return the user devices", notes = "", response = Device.class, nickname = "getDevices")
	public ResponseEntity<?> getDevices(@AuthenticationPrincipal User currentUser) {

		String deviceId = currentUser.getUuid();
		if (currentUser.isSuperAdmin()) {
			deviceId = null;
		}

		List<Device> devices = deviceService.list(deviceId);

		return ResponseEntity.ok(devices);
	}

	@RequestMapping(method = RequestMethod.POST)
	@ApiOperation(value = "Create a device instance", notes = "", response = Device.class, nickname = "createDevice")
	@PreAuthorize("hasPermission(null, 'create')")
	public ResponseEntity<?> createDevice(@AuthenticationPrincipal User currentUser, @RequestBody Device device) {

		device.setDefaults();

		try {
			device.validate();
		} catch (RaptorComponent.ValidationException ex) {
			return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST,
					"Device definition is not valid: " + ex.getMessage());
		}

		if (!currentUser.isSuperAdmin() || (device.userId() == null || device.userId().isEmpty())) {
			device.userId(currentUser.getUuid());
		}

		deviceService.save(device);

		eventPublisher.create(device);

		return ResponseEntity.ok(device.toJSON());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{deviceId}")
	@ApiOperation(value = "Return a device instance definition", notes = "", response = Device.class, nickname = "getDevice")
	@PostAuthorize("hasPermission(returnObject, 'read')")
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
	@PreAuthorize("hasPermission(#deviceId, 'update')")
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

		// reset ids
		// device.id(deviceId);
		// device.userId(body.userId());

		if (!currentUser.isSuperAdmin() || (device.userId() == null || device.userId().isEmpty())) {
			device.userId(currentUser.getUuid());
		}
		device.validate();

		deviceService.save(device);

		eventPublisher.update(device);

		return ResponseEntity.ok(device.toJSON());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{deviceId}")
	@ApiOperation(value = "Delete a device instance", notes = "", response = Device.class, nickname = "deleteDevice")
	@PreAuthorize("hasPermission(#deviceId, 'delete')")
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
	@PreAuthorize("hasPermission(null, 'list')")
	// @PostAuthorize("hasPermission(returnObject.body, 'read')") // TODO: enable
	// shared access
	public ResponseEntity<?> searchDevices(@AuthenticationPrincipal User currentUser,
			@RequestParam MultiValueMap<String, String> parameters, @RequestBody DeviceQuery query) {

		if (query.isEmpty()) {
			return JsonErrorResponse.badRequest();
		}

		if (!currentUser.isSuperAdmin()) {
			query.userId(currentUser.getUuid());
		}

		DeviceQueryBuilder qb = new DeviceQueryBuilder(query);
		Predicate predicate = qb.getPredicate();
		Pageable paging = qb.getPaging();

		Page<Device> pagedList = deviceService.search(predicate, paging);

		return ResponseEntity.ok(pagedList.getContent());
	}

}
