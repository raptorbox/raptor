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
package org.createnet.raptor.common.dispatcher;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class BrokerClient extends MqttPahoMessageHandler {

    final Logger log = LoggerFactory.getLogger(BrokerClient.class);
    
    
    
    public BrokerClient(String clientId, MqttPahoClientFactory clientFactory) {
        super(clientId, clientFactory);
        this.setAsync(false);
    }
    
    public BrokerClient(String url, String clientId, MqttPahoClientFactory clientFactory) {
        super(url, clientId, clientFactory);
        this.setAsync(false);
    }
    
    public void sendMessage(String topic, String message) {
        
        int retry = 0, max = 5;
        while(retry < max) {
            try {
                
                log.debug("Publishing to [topic={}]", topic);
                
                MqttMessage payload = new MqttMessage(message.getBytes());
                publish(topic, payload, null);
                
                log.debug("Published correctly [topic={}]", topic);
                return;
                
            } catch(Exception ex) {
                log.warn("Error on publish [topic={} retry={}/{}] {}", topic, retry, max, ex.getMessage());
                try {
                    Thread.sleep(500 * retry);
                } catch (InterruptedException ex1) {}
                retry ++;
            }
        }
        log.warn("Publishing failed after {} tries", retry);
    }
    
}
