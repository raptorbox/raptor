
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
package org.createnet.raptor.tree;

import org.createnet.raptor.api.common.BaseApplication;
import org.createnet.raptor.sdk.Topics;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.integration.core.MessageProducer;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@SpringBootApplication(
        scanBasePackages = {"org.createnet.raptor.api.common", "org.createnet.raptor.tree"}
)
@EnableSpringDataWebSupport
@EnableMongoRepositories(basePackages = "org.createnet.raptor.tree")
public class Application extends BaseApplication {

    public static void main(String[] args) {
        start(Application.class, args);
    }

    @Bean
    TreeMessageHandler treeMessageHandler() {
        return new TreeMessageHandler();
    }

    @Bean
    public MessageProducer mqttClient() {
        return createMqttClient(new String[] { String.format(Topics.DEVICE, "+") }, treeMessageHandler());
    }

}
