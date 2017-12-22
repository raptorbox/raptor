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
package org.createnet.raptor.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.createnet.raptor.sdk.config.Config;
import org.createnet.raptor.sdk.events.MqttEventEmitter;
import org.createnet.raptor.sdk.exception.ClientException;
import org.createnet.raptor.models.objects.RaptorContainer;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
abstract public class AbstractClient implements IClient {

    private final Raptor container;

    public AbstractClient(Raptor container) {
        this.container = container;
    }
    
    /**
     * @param offset
     * @param limit
     * @return 
     */
    @Deprecated
    protected String buildQueryString(Integer offset, Integer limit) {
        String qs = null;
        if (offset != null) {
            qs = "?offset=" + offset;
        }
        if (limit != null) {
            if (qs == null) {
                qs = "?";
            } else {
                qs += "&";
            }
            qs += "limit=" + limit;
        }
        return qs == null ? "" : qs;
    }

    @Override
    public Raptor getContainer() {
        return this.container;
    }

    @Override
    public HttpClient getClient() {
        return getContainer().getClient();
    }

    @Override
    public Config getConfig() {
        return getContainer().getConfig();
    }

    @Override
    public MqttEventEmitter getEmitter() {
        return getContainer().getEmitter();
    }

    public ObjectMapper getMapper() {
        return RaptorContainer.getMapper();
    }

    /**
     * Convert an object to JSON
     *
     * @param o
     * @return
     */
    public String toJson(Object o) {
        try {
            return getMapper().writeValueAsString(o);
        } catch (JsonProcessingException ex) {
            throw new ClientException(ex);
        }
    }

    /**
     * Convert an object to JsonNode
     *
     * @param o
     * @return
     */
    public JsonNode toJsonNode(Object o) {
        return getMapper().valueToTree(o);
    }
}
