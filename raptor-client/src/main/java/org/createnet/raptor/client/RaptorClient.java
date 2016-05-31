/*
 * Copyright 2016 Luca Capra <lcapra@create-net.org>.
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
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.createnet.raptor.client.event.MessageEventListener;
import org.createnet.raptor.client.exception.ClientException;
import org.createnet.raptor.client.model.ServiceObject;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class RaptorClient implements RaptorComponent {

  static {
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

  final public RaptorConfig config;
  protected RaptorClient client;

  public enum Event {
    DATA, OBJECT
  }
  
  public static class RaptorConfig {

    public String apiKey;
    public String url = "https://api.raptorbox.eu";
  }

  public static class Routes {

    final static public String LIST = "/";
    final static public String FIND = "/search";

    final static public String CREATE = "/";
    final static public String UPDATE = "/{id}";
    final static public String LOAD = "/{id}";
    final static public String DELETE = "/{id}";

  }

  public RaptorClient(RaptorConfig config) {
    this.config = config;
  }

  public String url(String path) {
    return config.url + path;
  }

  public ServiceObject load(String id) throws ClientException {
    return createObject().load(id);
  }

  public ServiceObject createObject() {
    ServiceObject obj = new ServiceObject();
    obj.setContainer(this);
    return obj;
  }

  public MqttClient getMqttClient() throws ClientException {
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

  public void unsubscribe(String topic) throws ClientException {
    try {
      getMqttClient().unsubscribe(topic);
    } catch (MqttException ex) {
      throw new ClientException(ex);
    }
  }
  
  public void subscribe(String topic, MessageEventListener listener) throws ClientException {
    try {
      
      logger.debug("Subscribing to MQTT topic {}", topic);
      getMqttClient().subscribe(topic);
      getMqttClient().setCallback(new MqttCallback() {
        @Override
        public void connectionLost(Throwable thrwbl) {
          logger.warn("Connection to MQTT server lost", thrwbl);
          try {
            getMqttClient().connect();
          } catch (MqttException | ClientException ex) {
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

  @Override
  public RaptorClient getClient() {
    return this;
  }

  @Override
  public void setClient(RaptorClient client) {
    this.client = client;
  }

}
