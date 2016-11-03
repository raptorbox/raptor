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
package org.createnet.raptor.client.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;
import org.createnet.raptor.client.RaptorClient;
import org.createnet.raptor.client.RaptorComponent;
import org.createnet.raptor.client.event.MessageEventListener;
import org.createnet.raptor.client.exception.ClientException;
import org.createnet.raptor.dispatcher.payload.ActionPayload;
import org.createnet.raptor.dispatcher.payload.DataPayload;
import org.createnet.raptor.dispatcher.payload.DispatcherPayload;
import org.createnet.raptor.dispatcher.payload.ObjectPayload;
import org.createnet.raptor.dispatcher.payload.StreamPayload;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.search.query.impl.es.ObjectQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods to interact with Raptor API
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ServiceObjectClient extends AbstractClient {

    final static Logger logger = LoggerFactory.getLogger(ServiceObjectClient.class);

    public interface ServiceObjectCallback {

        public static class PayloadMessage {

            final public ServiceObject object;
            final public JsonNode raw;
            final public DispatcherPayload payload;

            public PayloadMessage(ServiceObject object, JsonNode raw, DispatcherPayload payload) {
                this.raw = raw;
                this.payload = payload;
                this.object = object;
            }
        }

        public void execute(ServiceObject obj, PayloadMessage message);
    }

    protected String getTopic(ServiceObject obj) {
        return obj.id + "/events";
    }

    /**
     * Subscribe for events
     *
     * @param obj the object to listen for
     * @param callback The callback to fire on event arrival
     */
    public void subscribe(ServiceObject obj, ServiceObjectCallback callback) {
        getClient().subscribe(getTopic(obj), (MessageEventListener.Message message) -> {
            try {

                JsonNode json = ServiceObject.getMapper().readTree(message.content);
                DispatcherPayload payload = null;
                if (json.has("type")) {
                    Class<? extends DispatcherPayload> clazz = null;
                    switch (DispatcherPayload.MessageType.valueOf(json.get("type").asText())) {
                        case object:
                            clazz = ObjectPayload.class;
                            break;
                        case data:
                            clazz = DataPayload.class;
                            break;
                        case stream:
                            clazz = StreamPayload.class;
                            break;
                        case action:
                            clazz = ActionPayload.class;
                            break;
                    }

                    if (clazz != null) {
                        payload = ServiceObject.getMapper().convertValue(json, clazz);
                    }
                }

                callback.execute(obj, new ServiceObjectCallback.PayloadMessage(obj, json, payload));
            } catch (IOException ex) {
                logger.error("Error parsing JSON payload: {}", ex.getMessage());
            }
        });
    }

    /**
     * Unsubscribe a Stream for data updates
     *
     * @param obj obj from which to unsubscribe events
     */
    public void unsubscribe(ServiceObject obj) {
        getClient().unsubscribe(getTopic(obj));
    }

    /**
     * Create an object definition
     *
     * @param obj object definition to create
     * @return the ServiceObject instance
     */
    public ServiceObject create(ServiceObject obj) {
        JsonNode node = getClient().get(RaptorClient.Routes.CREATE);
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
     * @return the ServiceObject instance
     */
    public ServiceObject load(String id) {
        ServiceObject obj = getClient().createObject();
        obj.parse(
                getClient().get(
                        RaptorComponent.format(RaptorClient.Routes.LOAD, id)
                )
        );
        return obj;
    }

    /**
     * Update a ServiceObject instance
     *
     * @param obj the ServiceObject to update
     * @return the updated ServiceObject instance
     */
    public ServiceObject update(ServiceObject obj) {
        obj.parse(
                getClient().put(
                        RaptorComponent.format(RaptorClient.Routes.UPDATE, obj.getId()),
                        obj.toJsonNode()
                )
        );
        return obj;
    }

    /**
     * Search for ServiceObjects
     *
     * @param query the query to match the object definitions
     * @param offset results start from offset
     * @param limit limit the total size of result
     * @return a list of ServiceObjects matching the query
     */
    public List<ServiceObject> search(ObjectQuery query, Integer offset, Integer limit) {
        JsonNode json = getClient().post(
                RaptorClient.Routes.SEARCH,
                query.toJSON()
        );
        List<ServiceObject> results = ServiceObject.getMapper().convertValue(json, new TypeReference<List<ServiceObject>>() {
        });
        return results;
    }

    /**
     * Search for ServiceObjects
     *
     * @param query the query to match the object definitions
     * @return a list of ServiceObjects matching the query
     */
    public List<ServiceObject> search(ObjectQuery query) {
        return search(query, null, null);
    }

    /**
     * Delete a ServiceObject instance and all of its data
     *
     * @param obj ServiceObject to delete
     */
    public void delete(ServiceObject obj) {
        getClient().delete(
                RaptorComponent.format(RaptorClient.Routes.DELETE, obj.getId())
        );
        obj.id = null;
    }

}
