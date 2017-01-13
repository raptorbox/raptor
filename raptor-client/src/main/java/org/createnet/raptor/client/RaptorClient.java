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
package org.createnet.raptor.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import org.createnet.raptor.client.event.MessageEventListener;
import org.createnet.raptor.client.exception.ClientException;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.objects.ServiceObject;
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
 * @author Luca Capra <lcapra@create-net.org>
 */
public class RaptorClient implements IClient, RaptorComponent {

    static {

        Unirest.setDefaultHeader("content-type", "application/json");

        // Only one time
        Unirest.setObjectMapper(new ObjectMapper() {

            private final com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            @Override
            public <T> T readValue(String value, Class<T> valueType) {
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
    
    final protected Logger logger = LoggerFactory.getLogger(RaptorClient.class);

    protected MqttClient mqttClient;
    
    final protected State state;
    
    
    final public ClientConfig config;
    protected RaptorClient client;
    
    public enum Event {
        DATA, OBJECT
    }
    
    /**
     * Manage the state of the client
     */
    public class State {
        
        protected String token;
        protected boolean loggedIn = false;
        protected Instant lastLogin;
        protected User currentUser;

        public State() {
        
            this.token=config.token;
            
        }

        
        public void loggedIn(String token, User user) {
            loggedIn = true;
            lastLogin = Instant.now();
            currentUser = user;
            this.token = token;
        }

        public String getToken() {
            return token;
        }

    }
    
    /**
     * Configuration object
     */
    public static class ClientConfig {
        public String username;
        public String password;
        public String token;
        public String url = "http://api.raptor.local";
        public int loginExpiration = (60*60*5); // 5min
    }

    /**
     * List of base path for Raptor API
     */
    public static class Routes {

        final static public String LIST = "/";
        final static public String SEARCH = "/search";

        final static public String CREATE = LIST;
        final static public String UPDATE = "/{0}";
        final static public String LOAD = UPDATE;
        final static public String DELETE = UPDATE;

        final static public String PUSH = "/{0}/streams/{1}";
        final static public String LAST_UPDATE = PUSH;
        final static public String PULL = PUSH + "/list";
        final static public String SEARCH_DATA = PUSH + "/search";

        final static public String INVOKE = "/{0}/actions/{1}";
        final static public String ACTION_STATUS = INVOKE;
        final static public String ACTION_LIST = "/{0}/actions";

        final static public String SUBSCRIBE_STREAM = PUSH;
        
        final static public String LOGIN = "/auth/login";

    }

    public RaptorClient(ClientConfig config) {
        this.config = config;
        state = new State();
    }

    /**
     * @return the state of the client
     */
    public State getState() {
        return state;
    }
    
    @Override
    public RaptorClient getClient() {
        return this;
    }

    @Override
    public void setClient(RaptorClient client) {
        this.client = client;
    }

    /**
     * Add the configured base url to path
     *
     * @param path base path to create url from
     * @return a full url
     */
    public String url(String path) {
        return config.url + path;
    }

    /**
     * Creates a ServiceObject instance with container reference set
     *
     * @return the new ServiceObject instance
     */
    public ServiceObject createObject() {
        ServiceObject obj = new ServiceObject();
        obj.setContainer(this);
        return obj;
    }

    protected void prepareRequest() {
        
        if (getClient().getState().getToken() != null) {
            Unirest.setDefaultHeader("Authorization", "Bearer " + getClient().getState().getToken());
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

                URL url = new URL(config.url);
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
    @Override
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
    @Override
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
    @Override
    public JsonNode put(String url, JsonNode body) {
        try {
            prepareRequest();
            HttpResponse<JsonNode> objResponse = Unirest
                    .put(getClient().url(url))
                    .body(body)
                    .asObject(JsonNode.class);
            return objResponse.getBody();
        } catch (UnirestException ex) {
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
    @Override
    public JsonNode post(String url, JsonNode body) {
        try {
            prepareRequest();
            HttpResponse<JsonNode> objResponse = Unirest
                    .post(getClient().url(url))
                    .body(body)
                    .asObject(JsonNode.class);
            return objResponse.getBody();
        } catch (UnirestException ex) {
            throw new ClientException(ex);
        }
    }

    /**
     * Perform a GET request to the API
     *
     * @param url path of request
     * @return the request response
     */
    @Override
    public JsonNode get(String url) {
        try {
            prepareRequest();            
            HttpResponse<JsonNode> objResponse = Unirest
                    .get(getClient().url(url))
                    .asObject(JsonNode.class);
            return objResponse.getBody();
        } catch (UnirestException ex) {
            throw new ClientException(ex);
        }
    }

    /**
     * Perform a DELETE request to the API
     *
     * @param url path of request
     * @return the request response
     */
    @Override
    public JsonNode delete(String url) {
        try {
            prepareRequest();            
            HttpResponse<JsonNode> objResponse = Unirest
                    .delete(getClient().url(url))
                    .asObject(JsonNode.class);
            return objResponse.getBody();
        } catch (UnirestException ex) {
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
            HttpResponse<String> objResponse = Unirest
                    .post(getClient().url(path))
                    .header("content-type", "text/plain")
                    .body(payload)
                    .asString();

        } catch (UnirestException ex) {
            throw new ClientException(ex);
        }
    }

}
