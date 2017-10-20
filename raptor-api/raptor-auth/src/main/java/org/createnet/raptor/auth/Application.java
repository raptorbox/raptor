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
package org.createnet.raptor.auth;

import org.createnet.raptor.auth.services.AuthMessageHandler;
import org.createnet.raptor.common.BaseApplication;
import org.createnet.raptor.common.dispatcher.RaptorMessageHandlerWrapper;
import org.createnet.raptor.sdk.Topics;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.integration.core.MessageProducer;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Profile("default")
@SpringBootApplication(scanBasePackages = {"org.createnet.raptor.common", "org.createnet.raptor.auth"})
@EntityScan(basePackages = "org.createnet.raptor.models.auth")
@EnableScheduling
@EnableRetry
@EnableWebMvc
public class Application extends BaseApplication {

    public static void main(String[] args) {
        start(Application.class, args);
    }

    @Autowired AuthMessageHandler authMessageHandler;

    @Autowired
    RaptorMessageHandlerWrapper raptorMessageHandlerWrapper;

    @EventListener({ContextRefreshedEvent.class})
    void contextRefreshedEvent() {
        raptorMessageHandlerWrapper.registerHandler(authMessageHandler);
    }

    @Bean
    public MessageProducer mqttClient() {
        return createMqttClient(new String[]{String.format(Topics.DEVICE, "+")});
    }

}
