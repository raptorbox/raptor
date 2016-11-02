/*
 * Copyright 2016 Luca Capra <luca.capra@create-net.org>.
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
package org.createnet.raptor.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.createnet.raptor.client.event.MessageEventListener;

/**
 *
 * @author Luca Capra <luca.capra@create-net.org>
 */
public interface IClient {

    /**
     * Perform a DELETE request to the API
     *
     * @param url path of request
     * @return the request response
     */
    JsonNode delete(String url);

    /**
     * Perform a GET request to the API
     *
     * @param url path of request
     * @return the request response
     */
    JsonNode get(String url);

    /**
     * Perform a POST request to the API
     *
     * @param url path of request
     * @param body content to be sent
     * @return the request response
     */
    JsonNode post(String url, JsonNode body);

    /**
     * Perform a PUT request to the API
     *
     * @param url path of request
     * @param body content to be sent
     * @return the request response
     */
    JsonNode put(String url, JsonNode body);

    /**
     * Subscribe to an MQTT topic emitting a callback as specified in MessageEventListener
     *
     * @param topic the topic to listen for
     * @param listener the listener implementation
     */
    void subscribe(String topic, MessageEventListener listener);

    /**
     * Unsubscribe from an MQTT topic
     *
     * @param topic the topic to listen for
     */
    void unsubscribe(String topic);
    
}
