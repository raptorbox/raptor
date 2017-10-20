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
package org.createnet.raptor.common.configuration;

import static org.createnet.raptor.common.BaseApplication.log;
import org.createnet.raptor.common.dispatcher.BrokerClient;
import org.createnet.raptor.common.dispatcher.RaptorMessageHandlerWrapper;
import org.createnet.raptor.models.configuration.AuthConfiguration;
import org.createnet.raptor.models.configuration.DispatcherConfiguration;
import org.createnet.raptor.models.configuration.RaptorConfiguration;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@Configuration
public class MqttConfiguration {

    @Autowired
    RaptorConfiguration raptorConfiguration;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {

        AuthConfiguration.AdminUser defaultUser = raptorConfiguration.getAuth().getServiceUser();

        if (defaultUser == null) {
            throw new RuntimeException("Missing service user. Review raptor.yml configuration file under auth.users section");
        }

        DispatcherConfiguration dispatcherConfig = raptorConfiguration.getDispatcher();

        DefaultMqttPahoClientFactory f = new DefaultMqttPahoClientFactory();

        log.debug("Using local broker user {}", defaultUser.getUsername());

        f.setUserName(defaultUser.getUsername());
        f.setPassword(defaultUser.getPassword());
        f.setServerURIs(dispatcherConfig.getUri());
        f.setCleanSession(true);
        f.setPersistence(new MemoryPersistence());

        return f;
    }

    // Add inbound MQTT support
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Autowired
    RaptorMessageHandlerWrapper raptorMessageHandlerWrapper;

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                if (raptorMessageHandlerWrapper != null) {
                    try {
                        DispatcherPayload payload = DispatcherPayload.parseJSON(message.getPayload().toString());
                        raptorMessageHandlerWrapper.handle(payload, message.getHeaders());
                    } catch (Exception e) {
                        throw new MessagingException("Exception handling message", e);
                    }
                }
            }
        };
    }

    @Bean
    protected BrokerClient brokerClient() {
        String clientId = "raptor" + (System.currentTimeMillis() + Math.random());
        return new BrokerClient(raptorConfiguration.getDispatcher().getUri(), clientId, mqttClientFactory());
    }
    
}
