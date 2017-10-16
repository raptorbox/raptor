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
package org.createnet.raptor.sdk.events;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.createnet.raptor.sdk.AbstractClient;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.exception.ClientException;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class MqttClientHandler extends AbstractClient {

    final private Logger logger = LoggerFactory.getLogger(MqttClientHandler.class);
    protected org.eclipse.paho.client.mqttv3.MqttClient mqttClient;

    final private List<String> topics = new ArrayList();

    public MqttClientHandler(Raptor container) {
        super(container);
    }

    /**
     * Return and lazily creates an MQTT client
     *
     * @return the MQTT client instance
     */
    public org.eclipse.paho.client.mqttv3.MqttClient getMqttClient() {

        if (mqttClient == null) {
            try {

                URL url = new URL(getConfig().getUrl());
                String clientId = "raptorclient" + ((int) (System.currentTimeMillis() / 1000));
                String serverURI = "tcp://" + url.getHost() + ":1883";

                mqttClient = new org.eclipse.paho.client.mqttv3.MqttClient(serverURI, clientId, new MemoryPersistence());

            } catch (MqttException | MalformedURLException ex) {
                throw new ClientException(ex);
            }
        }

        if (!mqttClient.isConnected()) {

            try {

                logger.debug("Connecting to MQTT server {}", mqttClient.getServerURI());

                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                connOpts.setConnectionTimeout(5); // 5 secs

                if (getContainer().getConfig().hasCredentials()) {
                    connOpts.setUserName(getContainer().getConfig().getUsername());
                    connOpts.setPassword(getContainer().getConfig().getPassword().toCharArray());
                    logger.debug("Using user credentials");
                } else if (getContainer().getConfig().hasToken()) {
                    connOpts.setUserName("**"); // username  len <= 3 trigger token authentication
                    connOpts.setPassword(getContainer().getConfig().getToken().toCharArray());
                    logger.debug("Using user token");
                } else {
                    logger.debug("No token or credentials available for connection, ignoring");
                    return mqttClient;
                }

                mqttClient.connect(connOpts);

            } catch (MqttException ex) {
                logger.error("Connection failed", ex);
                throw new ClientException(ex);
            }
            
            logger.debug("MQTT client connected={}", mqttClient.isConnected());
            
        }
        return mqttClient;
    }

    /**
     * Subscribe to an MQTT topic
     *
     * @param topic the topic to listen for
     */
    public void subscribe(String topic) {

        try {
            if (topics.contains(topic)) {
                logger.debug("MQTT topic is already subscribed{}", topic);
                return;
            }

            logger.debug("Subscribing to MQTT topic {}", topic);

            getMqttClient().subscribe(topic);
            topics.add(topic);

            logger.debug("Subscribed");
        } catch (MqttException ex) {

            // Disconnection may be caused by lack of permissions
            int code = ex.getReasonCode();
            if (code == MqttException.REASON_CODE_CONNECTION_LOST) {
                logger.warn("Client disconnected, try checking user / token permission to access the topic {}", topic);
            }

            throw new ClientException(ex);
        }
    }

    /**
     * Set the callback to trigger on message arrival
     *
     * @param listener the listener implementation
     */
    public void setCallback(MessageEventListener listener) {

        if (listener == null) {
            logger.debug("Clear callback");
            getMqttClient().setCallback(null);
            return;
        }

        logger.debug("Set callback");
        getMqttClient().setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable thrwbl) {
                logger.warn("Connection to MQTT server lost, reconnecting", thrwbl);
                if (!getMqttClient().isConnected()) {
                    mqttClient = null;
                    getMqttClient();
                }
            }

            @Override
            public void messageArrived(String mqttTopic, MqttMessage mqttMessage) throws Exception {

                MessageEventListener.Message message = new MessageEventListener.Message();
                message.topic = mqttTopic;
                message.content = new String(mqttMessage.getPayload());

                logger.debug("New message received on {}", message.topic, message.content);
                listener.onMessage(message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken imdt) {

            }
        });
    }

    /**
     * Unsubscribe from an MQTT topic
     *
     * @param topic the topic to listen for
     */
    public void unsubscribe(String topic) {
        try {
            getMqttClient().unsubscribe(topic);
            topics.remove(topic);
            if (topics.isEmpty()) {
                mqttClient.setCallback(null);
            }
        } catch (MqttException ex) {
            throw new ClientException(ex);
        }
    }

    /**
     * Disconnect from the MQTT broker
     */
    public void disconnect() {
        try {
            topics.clear();
            if (getMqttClient().isConnected()) {
                getMqttClient().disconnect();
            }
        } catch (MqttException ex) {
            throw new ClientException(ex);
        }
    }

}
