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
package org.createnet.raptor.client.api;

import org.createnet.raptor.client.AbstractClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;
import org.createnet.raptor.client.Raptor;
import org.createnet.raptor.client.event.MessageEventListener;
import org.createnet.raptor.client.exception.ClientException;
import org.createnet.raptor.client.exception.MissingAuthenticationException;
import org.createnet.raptor.models.payload.ActionPayload;
import org.createnet.raptor.models.payload.DataPayload;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.createnet.raptor.models.payload.ObjectPayload;
import org.createnet.raptor.models.payload.StreamPayload;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.indexer.query.impl.es.ObjectQuery;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods to interact with Raptor API
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class DeviceClient extends AbstractClient {

    public DeviceClient(Raptor container) {
        super(container);
    }

    final static Logger logger = LoggerFactory.getLogger(DeviceClient.class);

    public static class PayloadMessage {

        final public Device object;
        final public DispatcherPayload payload;

        public PayloadMessage(Device object, DispatcherPayload payload) {
            this.payload = payload;
            this.object = object;
        }
    }

   
    public interface DataCallback {
        public void callback(Stream stream, StreamPayload message);
    }
    
    public interface ActionCallback {
        public void callback(Action action, ActionPayload message);
    }
    
    public interface StreamCallback {
        public void callback(Stream stream, StreamPayload message);
    }
    
    public interface DeviceCallback {
        public void callback(Device obj, ObjectPayload message);
    }

    public interface EventCallback extends ActionCallback, StreamCallback, DeviceCallback, DataCallback {
        public void callback(Device obj, PayloadMessage message);
    }
    
    
    protected String getTopic(Device obj) {
        return obj.id + "/events";
    }

    /**
     * Subscribe for events
     *
     * @param obj the object to listen for
     * @param callback The callback to fire on event arrival
     */
    public void subscribe(Device obj, EventCallback callback) {
        getClient().subscribe(getTopic(obj), (MessageEventListener.Message message) -> {

            DispatcherPayload payload = DispatcherPayload.parseJSON(message.content);
            switch (payload.getType()) {
                case object:
                    callback.callback(obj, (ObjectPayload)payload);
                    break;
                case data:
                    callback.callback(obj, (ObjectPayload)payload);
                    break;
                case stream:
                    StreamPayload streamPayload = (StreamPayload)payload;
                    callback.callback(obj.getStream(streamPayload.streamId), streamPayload);
                    break;
                case action:
                    ActionPayload actionPayload = (ActionPayload)payload;
                    callback.callback(obj.getAction(actionPayload.actionId), actionPayload);
                    break;
            }
            
            callback.callback(obj, new PayloadMessage(obj, payload));
        });
    }

    /**
     * Unsubscribe a Stream for data updates
     *
     * @param obj obj from which to unsubscribe events
     */
    public void unsubscribe(Device obj) {
        getClient().unsubscribe(getTopic(obj));
    }

    /**
     * Create an object definition
     *
     * @param obj object definition to create
     * @return the Device instance
     */
    public Device create(Device obj) {
        JsonNode node = getClient().post(Client.Routes.CREATE, obj.toJsonNode());
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
        Device obj = getClient().createObject();
        obj.parse(
                getClient().get(
                        String.format(Client.Routes.LOAD, id)
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
                        String.format(Client.Routes.UPDATE, obj.getId()),
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
            User user = getContainer().Auth.getUser();
            if (user == null) {
                throw new MissingAuthenticationException("User is not available");
            }
            query.setUserId(user.getUuid());
        }
        JsonNode json = getClient().post(
                Client.Routes.SEARCH,
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
                String.format(Client.Routes.DELETE, obj.getId())
        );
        obj.id = null;
    }

    /**
     * List accessible devices
     *
     * @return the Device instance
     */
    public List<Device> list() {
        JsonNode json = getClient().get(Client.Routes.LIST);
        List<Device> list = Device.getMapper().convertValue(json, new TypeReference<List<Device>>() {
        });
        return list;
    }

}
