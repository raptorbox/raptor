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
package org.createnet.raptor.dispatcher.client;

import org.createnet.raptor.config.Configuration;
import org.createnet.raptor.dispatcher.configuration.DispatcherConfiguration;
import org.createnet.raptor.plugin.PluginConfiguration;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class MqttBrokerClient implements BrokerClient<DispatcherConfiguration> {
    
  Logger logger = LoggerFactory.getLogger(MqttBrokerClient.class);
  
  private static MqttClient connection = null;
  private DispatcherConfiguration configuration;
    
  private final String clientName = "raptor-dispatcher";
  private final int connectionTimeout = 10;
  private final MemoryPersistence clientPersistence = new MemoryPersistence();
  
  private final int qos = 2;
  private final boolean retain = false;

  @Override
  public void initialize(DispatcherConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public synchronized void connect() throws BrokerClientException {
    
    if(connection != null && connection.isConnected()) {
      logger.debug("Mqtt connection available");
      return;
    }
    
    logger.debug("Connecting to mqtt broker");
    
    try {

      String uri = configuration.mqtt.uri;
      String username = configuration.mqtt.username;
      String password = configuration.mqtt.password;
      
      logger.debug("Connecting to broker {}", uri);
      
      connection = new MqttClient(uri, clientName, clientPersistence);
      
      MqttConnectOptions connOpts = new MqttConnectOptions();
      
      connOpts.setCleanSession(true);
      connOpts.setConnectionTimeout(connectionTimeout);
      
      if(username != null && password != null) {
        connOpts.setUserName(username);
        connOpts.setPassword(password.toCharArray());
      }
      
      connection.connect(connOpts);
      
    } catch (MqttException me) {
//      logger.error("Failed to connect to broker", me);
      logger.error("Failed to connect to broker", me);
      throw new BrokerClientException(me);
    }
  }
  
  public MqttClient getConnection() throws BrokerClientException {
    connect();
    return connection;
  }
  
  @Override
  public void send(String topic, String message) throws BrokerClientException {
    try {
      
      MqttClient conn = getConnection();
      if(conn == null || !conn.isConnected() ) {
        throw new BrokerClientException("Connection is not available");
      }
      
      getConnection().publish(topic, message.getBytes(), qos, retain);
    }
    catch(MqttException e) {
      logger.error("MQTT exception", e);
      throw new BrokerClientException(e);
    }
  }
  
  @Override
  public void disconnect() {
    if(connection != null && connection.isConnected()) {
      try {
        connection.disconnect();
      } catch (MqttException ex) {
        logger.error("Cannot close connection properly", ex);
      }
    }
  }

  @Override
  public boolean isConnected() {
    return connection == null ? false : connection.isConnected();
  }
  
  @Override
  public PluginConfiguration getPluginConfiguration() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Configuration getConfiguration() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void initialize(Configuration configuration) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
