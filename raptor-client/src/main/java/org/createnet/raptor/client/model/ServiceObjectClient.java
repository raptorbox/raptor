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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.createnet.raptor.client.RaptorClient;
import org.createnet.raptor.client.RaptorComponent;
import org.createnet.raptor.client.exception.ClientException;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.search.query.impl.es.ObjectQuery;

/**
 * Represent a virtual object
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ServiceObjectClient extends AbstractClient {

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
