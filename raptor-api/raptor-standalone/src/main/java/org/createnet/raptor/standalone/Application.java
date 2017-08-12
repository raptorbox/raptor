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
package org.createnet.raptor.standalone;

import java.util.Arrays;
import org.createnet.raptor.auth.services.AuthMessageHandler;
import org.createnet.raptor.common.BaseApplication;
import org.createnet.raptor.sdk.Topics;
import org.createnet.raptor.tree.TreeMessageHandler;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.integration.core.MessageProducer;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Profile("default")
@SpringBootApplication(scanBasePackages = {
    "org.createnet.raptor",
}, exclude = {
    ArtemisAutoConfiguration.class
})

@EntityScan(basePackages = "org.createnet.raptor.models.auth")
@EnableJpaRepositories(basePackages = "org.createnet.raptor.auth.repository")

@EnableMongoRepositories(basePackages = {
    "org.createnet.raptor.stream",
    "org.createnet.raptor.action",
    "org.createnet.raptor.inventory",
    "org.createnet.raptor.profile",    
    "org.createnet.raptor.tree",
})

@EnableWebMvc
@ComponentScan(basePackages = {
    "org.createnet.raptor"
})

@EnableScheduling
@EnableRetry
@WebAppConfiguration
public class Application extends BaseApplication {
    
    public static void main(String[] args) {
        additionalConfigNames = Arrays.asList(
                "auth.yml"
        );
        start(Application.class, args);
    }
   
    @Bean
    TreeMessageHandler treeMessageHandler() {
        return new TreeMessageHandler();
    }
    
    @Bean
    AuthMessageHandler authMessageHandler(){
        return new AuthMessageHandler();
    }
    
    @Bean
    MultipleMessageHandler multipleMessageHandler() {
        MultipleMessageHandler m = new MultipleMessageHandler();
        m.addHandler(treeMessageHandler());
        m.addHandler(authMessageHandler());
        return m;
    }
    
    @Bean
    public MessageProducer mqttClient() {
        return createMqttClient(new String[]{
            String.format(Topics.DEVICE, "+"),}, multipleMessageHandler());
    }    
    
    
}
