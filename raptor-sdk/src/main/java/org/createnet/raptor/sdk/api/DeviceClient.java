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
package org.createnet.raptor.sdk.api;

import org.createnet.raptor.sdk.AbstractClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.events.callback.ActionCallback;
import org.createnet.raptor.sdk.events.callback.DataCallback;
import org.createnet.raptor.sdk.events.callback.DeviceCallback;
import org.createnet.raptor.sdk.events.callback.DeviceEventCallback;
import org.createnet.raptor.sdk.events.callback.StreamCallback;
import org.createnet.raptor.sdk.exception.ClientException;
import org.createnet.raptor.sdk.exception.MissingAuthenticationException;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.createnet.raptor.models.payload.DevicePayload;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.indexer.query.impl.es.ObjectQuery;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.payload.ActionPayload;
import org.createnet.raptor.models.payload.StreamPayload;
import org.createnet.raptor.sdk.admin.DevicePermissionClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods to interact with Raptor API
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class DeviceClient extends AbstractClient {

    protected DevicePermissionClient Permission;
    
    public DevicePermissionClient Permission() {
        if (Permission == null) {
            Permission = new DevicePermissionClient(getContainer());
        }
        return Permission;
    }    
    
    public DeviceClient(Raptor container) {
        super(container);
        Permission = new DevicePermissionClient(container);
    }

    final static Logger logger = LoggerFactory.getLogger(DeviceClient.class);

    /**
     * Register for device events
     *
     * @param device the device to listen for
     * @param callback The callback to fire on event arrival
     */
    public void subscribe(Device device, DeviceEventCallback callback) {
        getEmitter().subscribe(device, callback);
    }

    /**
     * Subscribe only to device related events like update or delete
     *
     * @param dev
     * @param ev
     */
    public void subscribe(Device dev, DeviceCallback ev) {
        getEmitter().subscribe(dev, (DispatcherPayload payload) -> {
            switch (payload.getType()) {
                case object:
                    ev.callback(dev, (DevicePayload) payload);
                    break;
            }
        });
    }

    /**
     * Subscribe only to stream related events like update, push or delete
     *
     * @param dev
     * @param ev
     */
    public void subscribe(Device dev, StreamCallback ev) {
        getEmitter().subscribe(dev, (DispatcherPayload payload) -> {
            switch (payload.getType()) {
                case stream:
                    StreamPayload spayload = (StreamPayload) payload;
                    ev.callback(dev.getStream(spayload.streamId), spayload);
                    break;
            }
        });
    }

    /**
     * Subscribe only to action related events like invoke or status change
     *
     * @param dev
     * @param ev
     */
    public void subscribe(Device dev, ActionCallback ev) {
        getEmitter().subscribe(dev, (DispatcherPayload payload) -> {
            switch (payload.getType()) {
                case action:
                    ActionPayload apayload = (ActionPayload) payload;
                    ev.callback(dev.getAction(apayload.actionId), apayload);
                    break;
            }
        });
    }

    /**
     * Subscribe only to data updates
     *
     * @param dev
     * @param ev
     */
    public void subscribe(Device dev, DataCallback ev) {
        getEmitter().subscribe(dev, new DeviceEventCallback() {
            @Override
            public void trigger(DispatcherPayload payload) {
                switch (payload.getType()) {
                    case stream:
                        if(!payload.getOp().equals("data")) {
                            return;
                        }
                        StreamPayload dpayload = (StreamPayload) payload;
                        RecordSet record = RecordSet.fromJSON(dpayload.data);
                        ev.callback(dev.getStream(record.streamId), record);
                        break;
                }
            }
        });
    }

    /**
     * Create an object definition
     *
     * @param obj object definition to create
     * @return the Device instance
     */
    public Device create(Device obj) {
        JsonNode node = getClient().post(HttpClient.Routes.CREATE, obj.toJsonNode());
        if (!node.has("id")) {
            throw new ClientException("Missing ID on object creation");
        }
        obj.id = node.get("id").asText();
        return obj;
    }

    /**
     * Load an object definition
     *
     * @param id unique id of the object
     * @return the Device instance
     */
    public Device load(String id) {
        Device obj = new Device();
        obj.parse(
                getClient().get(
                        String.format(HttpClient.Routes.LOAD, id)
                )
        );
        return obj;
    }

    /**
     * Update a Device instance
     *
     * @param obj the Device to update
     * @return the updated Device instance
     */
    public Device update(Device obj) {
        obj.parse(
                getClient().put(
                        String.format(HttpClient.Routes.UPDATE, obj.getId()),
                        obj.toJsonNode()
                )
        );
        return obj;
    }

    /**
     * Search for Devices
     *
     * @param query the query to match the object definitions
     * @param offset results start from offset
     * @param limit limit the total size of result
     * @return a list of Devices matching the query
     */
    public List<Device> search(ObjectQuery query, Integer offset, Integer limit) {
        if (query.getUserId() == null) {
            User user = getContainer().Auth().getUser();
            if (user == null) {
                throw new MissingAuthenticationException("User is not available");
            }
            query.setUserId(user.getUuid());
        }
        JsonNode json = getClient().post(
                HttpClient.Routes.SEARCH,
                query.toJSON()
        );
        List<Device> results = Device.getMapper().convertValue(json, new TypeReference<List<Device>>() {
        });
        return results;
    }

    /**
     * Search for Devices
     *
     * @param query the query to match the object definitions
     * @return a list of Devices matching the query
     */
    public List<Device> search(ObjectQuery query) {
        return search(query, null, null);
    }

    /**
     * Delete a Device instance and all of its data
     *
     * @param obj Device to delete
     */
    public void delete(Device obj) {
        getClient().delete(
                String.format(HttpClient.Routes.DELETE, obj.getId())
        );
        obj.id = null;
    }

    /**
     * List accessible devices
     *
     * @return the Device instance
     */
    public List<Device> list() {
        JsonNode json = getClient().get(HttpClient.Routes.LIST);
        List<Device> list = Device.getMapper().convertValue(json, new TypeReference<List<Device>>() {
        });
        return list;
    }

}
