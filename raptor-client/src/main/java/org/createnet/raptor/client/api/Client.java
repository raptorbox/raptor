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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.createnet.raptor.client.Raptor;
import org.createnet.raptor.client.event.MessageEventListener;
import org.createnet.raptor.client.exception.ClientException;
import org.createnet.raptor.client.exception.MissingAuthenticationException;
import org.createnet.raptor.models.exception.RequestException;
import org.createnet.raptor.models.objects.Device;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client class for MQTT and HTTP operations
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class Client extends AbstractClient {

    static {

        Unirest.setDefaultHeader("content-type", "application/json");

        // Only one time
        Unirest.setObjectMapper(new ObjectMapper() {

            private final com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();
            
            @Override
            public <T> T readValue(String value, Class<T> valueType) {
                if (value == null ) {
                    return null;
                }
                if (value.isEmpty()) {
                    return null;
                }
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    final protected Logger logger = LoggerFactory.getLogger(Client.class);

    protected MqttClient mqttClient;
    
    public Client(Raptor container) {
        super(container);
    }

    public enum Event {
        DATA, OBJECT
    }

    /**
     * List of base path for Raptor API
     */
    public static class Routes {

        final static public String LIST = "/";
        final static public String SEARCH = "/search";

        final static public String CREATE = LIST;
        final static public String UPDATE = "/%s";
        final static public String LOAD = UPDATE;
        final static public String DELETE = UPDATE;

        final static public String PUSH = "/%s/streams/%s";
        final static public String LAST_UPDATE = PUSH;
        final static public String PULL = PUSH + "/list";
        final static public String SEARCH_DATA = PUSH + "/search";

        final static public String INVOKE = "/%s/actions/%s";
        final static public String ACTION_STATUS = INVOKE;
        final static public String ACTION_LIST = "/%s/actions";

        final static public String SUBSCRIBE_STREAM = PUSH;

        final static public String LOGIN = "/auth/login";
        final static public String LOGOUT = LOGIN;
        final static public String REFRESH_TOKEN = "/auth/refresh";

    }

    /**
     * Add the configured base url to path
     *
     * @param path base path to create url from
     * @return a full url
     */
    public String url(String path) {
        return getConfig().getUrl() + path;
    }

    /**
     * Creates a Device instance with container reference set
     *
     * @return the new Device instance
     */
    public Device createObject() {
        Device obj = new Device();
        obj.setContainer(this.getContainer());
        return obj;
    }

    protected void prepareRequest() {

        String token = getContainer().Auth.getToken();

        if (token == null) {
            throw new MissingAuthenticationException("Token is not available");
        }

        Unirest.setDefaultHeader("Authorization", "Bearer " + token);
    }

    protected void checkResponse(HttpResponse<?> response) {

        int status = response.getStatus();

        if (status >= 400) {
            String message = response.getBody().toString();
            if(response.getBody() instanceof JsonNode) {
                JsonNode err = (JsonNode) response.getBody();
                if (err.has("message")) {
                    message = err.get("message").asText();
                }
            }
            logger.error("Request failed {} {}: {}", response.getStatus(), response.getStatusText(), message);
            throw new RequestException(response.getStatus(), response.getStatusText(), message);
        }

    }

    /**
     * Return and lazily creates an MQTT client
     *
     * @return the MQTT client instance
     */
    public MqttClient getMqttClient() {

        if (mqttClient == null) {
            try {

                URL url = new URL(getConfig().getUrl());
                String clientId = "raptor_" + (int) (System.currentTimeMillis() / 1000);
                String serverURI = "tcp://" + url.getHost() + ":1883";

                mqttClient = new MqttClient(serverURI, clientId, new MemoryPersistence());

            } catch (MqttException | MalformedURLException ex) {
                throw new ClientException(ex);
            }
        }

        if (!mqttClient.isConnected()) {
            try {
                logger.debug("Connecting to MQTT server {}", mqttClient.getServerURI());
                mqttClient.connect();
            } catch (MqttException ex) {
                logger.error("Connection failed", ex);
                throw new ClientException(ex);
            }
        }

        return mqttClient;
    }

    /**
     * Unsubscribe from an MQTT topic
     *
     * @param topic the topic to listen for
     */
    public void unsubscribe(String topic) {
        try {
            getMqttClient().unsubscribe(topic);
        } catch (MqttException ex) {
            throw new ClientException(ex);
        }
    }

    /**
     * Subscribe to an MQTT topic emitting a callback as specified in
     * MessageEventListener
     *
     * @param topic the topic to listen for
     * @param listener the listener implementation
     */
    public void subscribe(String topic, MessageEventListener listener) {
        try {

            logger.debug("Subscribing to MQTT topic {}", topic);
            getMqttClient().subscribe(topic);
            getMqttClient().setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable thrwbl) {
                    logger.warn("Connection to MQTT server lost", thrwbl);
                    try {
                        getMqttClient().connect();
                    } catch (MqttException ex) {
                        logger.error("Failed to reconnect", ex);
                    }
                }

                @Override
                public void messageArrived(String mqttTopic, MqttMessage mqttMessage) throws Exception {

                    MessageEventListener.Message message = new MessageEventListener.Message();
                    message.topic = mqttTopic;
                    message.content = new String(mqttMessage.getPayload());

                    logger.debug("New message received on {}: {}", message.topic, message.content);
                    listener.onMessage(message);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken imdt) {

                }
            });

        } catch (MqttException ex) {
            throw new ClientException(ex);
        }
    }

    /**
     * Perform a PUT request to the API
     *
     * @param url path of request
     * @param body content to be sent
     * @return the request response
     */
    public JsonNode put(String url, JsonNode body) {
        try {
            logger.debug("PUT {}", url);
            prepareRequest();
            HttpResponse<JsonNode> objResponse = Unirest
                    .put(getClient().url(url))
                    .body(body)
                    .asObject(JsonNode.class);
            checkResponse(objResponse);
            return objResponse.getBody();
        } catch (UnirestException ex) {
            logger.error("Request error: {}", ex.getMessage());
            throw new ClientException(ex);
        }
    }

    /**
     * Perform a POST request to the API
     *
     * @param url path of request
     * @param body content to be sent
     * @return the request response
     */
    public JsonNode post(String url, JsonNode body) {
        try {
            logger.debug("POST {}", url);
            // catch login url and skip token signing
            if (!url.equals(Routes.LOGIN)) {
                prepareRequest();
            }
            HttpResponse<JsonNode> objResponse = Unirest
                    .post(getClient().url(url))
                    .body(body)
                    .asObject(JsonNode.class);
            checkResponse(objResponse);
            return objResponse.getBody();
        } catch (UnirestException ex) {
            logger.error("Request error: {}", ex.getMessage());
            throw new ClientException(ex);
        }
    }

    /**
     * Perform a GET request to the API
     *
     * @param url path of request
     * @return the request response
     */
    public JsonNode get(String url) {
        try {
            logger.debug("GET {}", url);
            prepareRequest();
            HttpResponse<JsonNode> objResponse = Unirest
                    .get(getClient().url(url))
                    .asObject(JsonNode.class);
            checkResponse(objResponse);
            return objResponse.getBody();
        } catch (UnirestException ex) {
            logger.error("Request error: {}", ex.getMessage());
            throw new ClientException(ex);
        }
    }

    /**
     * Perform a DELETE request to the API
     *
     * @param url path of request
     * @return the request response
     */
    public JsonNode delete(String url) {
        try {
            logger.debug("DELETE {}", url);
            prepareRequest();
            HttpResponse<JsonNode> objResponse = Unirest
                    .delete(getClient().url(url))
                    .asObject(JsonNode.class);
            checkResponse(objResponse);
            return objResponse.getBody();
        } catch (UnirestException ex) {
            logger.error("Request error: {}", ex.getMessage());
            throw new ClientException(ex);
        }
    }

    /**
     * Send a text payload (specific for invoking actions)
     *
     * @param path path of request
     * @param payload the raw content to send
     */
    public void post(String path, String payload) {
        try {
            logger.debug("POST text/plain {}", path);
            HttpResponse<String> objResponse = Unirest
                    .post(getClient().url(path))
                    .header("content-type", "text/plain")
                    .body(payload)
                    .asString();
            checkResponse(objResponse);
        } catch (UnirestException ex) {
            logger.error("Request error: {}", ex.getMessage());
            throw new ClientException(ex);
        }
    }

}
