/*
 * The MIT License
 *
 * Copyright 2016 CREATE-NET
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.createnet.raptor.dispatcher;

import java.util.Map;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class BrokerClient {
    
  Logger logger = LoggerFactory.getLogger(BrokerClient.class);
  
  private static MqttAsyncClient connection = null;
  final private Map<String, String> configuration;
    
  private final String clientName = "raptor-dispatcher";
  private final int connectionTimeout = 10;
  private final MemoryPersistence clientPersistence = new MemoryPersistence();
  
  private final int qos = 2;
  private final boolean retain = false;

  
  public interface BrokerClientListener {
    
    public void onConnectSuccess();
    public void onConnectError(Throwable t);
    
    public void onPublishSuccess();    
    public void onPublishError(Throwable t);
  }
  
  private BrokerClientListener bclistener;

  public BrokerClientListener getListener() {
    return bclistener;
  }

  public boolean hasListener() {
    return getListener() != null;
  }

  public void setListener(BrokerClientListener bclistener) {
    this.bclistener = bclistener;
  }
  
  public BrokerClient(Map<String, String> config) {
    configuration = config;  
  }

  protected synchronized void connect() throws MqttException {
    
    if(connection != null && connection.isConnected()) {
      logger.debug("Mqtt connection available");
      return;
    }
    
    logger.debug("Connecting to mqtt broker");
    
    try {

      String uri = configuration.get("uri");      
      String username = configuration.get("username");
      String password = configuration.get("password");
      
      logger.debug("Connecting to broker {}", uri);

      
      connection = new MqttAsyncClient(uri, clientName, clientPersistence);
      
      MqttConnectOptions connOpts = new MqttConnectOptions();
      
      connOpts.setCleanSession(true);
      connOpts.setConnectionTimeout(connectionTimeout);
      
      if(username != null && password != null) {
        connOpts.setUserName(username);
        connOpts.setPassword(password.toCharArray());
      }
      
      connection.connect(connOpts, null, new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken imt) {
          logger.debug("Connected");          
          if(hasListener()) {
            getListener().onConnectSuccess();
          }
        }

        @Override
        public void onFailure(IMqttToken imt, Throwable thrwbl) {
          getListener().onConnectError(thrwbl);
        }
      });
      
    } catch (MqttException me) {
//      logger.error("Failed to connect to broker", me);
      logger.error("Failed to connect to broker", me);
      throw me;
    }
  }
  
  public MqttAsyncClient getConnection() throws MqttException {
    connect();
    return connection;
  }

  public void sendMessage(String topic, String message, IMqttActionListener listener) throws DispatchException {
    try {
      
      MqttAsyncClient conn = getConnection();
      if(conn == null || !conn.isConnected() ) {
        return;
      }
      
      getConnection().publish(topic, message.getBytes(), qos, retain, new Object(), new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken imt) {
          listener.onSuccess(imt);
          if(hasListener()) {
            getListener().onPublishSuccess();
          }
        }

        @Override
        public void onFailure(IMqttToken imt, Throwable thrwbl) {
          listener.onFailure(imt, thrwbl);
          getListener().onPublishError(thrwbl);
        }
      });
    }
    catch(MqttException e) {
      logger.error("MQTT exception", e);
      throw new DispatchException();
    }
    catch(Exception e) {
      logger.error("Error sending MQTT message", e);
      throw new DispatchException();
    }
  }
  
  public void disconnect() {
    if(connection != null && connection.isConnected()) {
      try {
        connection.disconnect();
      } catch (MqttException ex) {
        logger.error("Cannot close connection properly", ex);
      }
    }
  }

  boolean isConnected() {
    return connection == null ? false : connection.isConnected();
  }
  
}
