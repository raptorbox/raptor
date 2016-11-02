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

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import org.createnet.raptor.client.RaptorClient;
import org.createnet.raptor.client.RaptorComponent;
import org.createnet.raptor.client.exception.ClientException;

/**
 * Represent a virtual object
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ServiceObject
        extends org.createnet.raptor.models.objects.ServiceObject
        implements RaptorComponent {

    private RaptorClient client;

    final public Map<String, Action> actions = new HashMap();
    final public Map<String, Stream> streams = new HashMap();    
    
    public ServiceObject() {
    }

    public ServiceObject(String id) {
        this.id = id;
    }

    /**
     * Load a ServiceObject from a JsonNode object
     *
     * @param json the object to read from
     * @return a ServiceObject instance
     */
    public static ServiceObject fromJSON(JsonNode json) {
        return (ServiceObject) org.createnet.raptor.models.objects.ServiceObject.fromJSON(json);
    }

    /**
     * Load a ServiceObject from a String
     *
     * @param json the object to read from
     * @return a ServiceObject instance
     */
    public static ServiceObject fromJSON(String json) {
        return (ServiceObject) org.createnet.raptor.models.objects.ServiceObject.fromJSON(json);
    }

    /**
     * Load an object definition
     *
     * @param id unique id of the object
     * @return the ServiceObject instance
     */
    public ServiceObject load(String id) {
        parse(
                getClient().get(
                        RaptorComponent.format(RaptorClient.Routes.LOAD, id)
                )
        );
        return this;
    }

    /**
     * Update a ServiceObject instance
     *
     * @return the updated ServiceObject instance
     */
    public ServiceObject update() {
        parse(
                getClient().put(
                        RaptorComponent.format(RaptorClient.Routes.UPDATE, this.getId()),
                        this.toJsonNode()
                )
        );
        return this;
    }

    /**
     * Delete a ServiceObject instance and all of its data
     */
    public void delete() {
        getClient().delete(
                RaptorComponent.format(RaptorClient.Routes.DELETE, this.getId())
        );
        this.id = null;
    }

    /**
     * Load a ServiceObject definition
     *
     * @return the ServiceObject instance
     */
    public ServiceObject load() {
        if (this.getId() == null) {
            throw new ClientException("ServiceObject is missing id, cannot load");
        }
        return load(this.getId());
    }

    @Override
    public RaptorClient getClient() {
        return this.client;
    }

    @Override
    public void setClient(RaptorClient client) {
        this.client = client;
    }

}
