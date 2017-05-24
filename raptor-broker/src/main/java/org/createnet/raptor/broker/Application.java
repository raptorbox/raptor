/*
 * Copyright 2017 Luca Capra <lcapra@fbk.eu>.
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
package org.createnet.raptor.broker;

import org.createnet.raptor.api.common.BaseApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@SpringBootApplication(
        scanBasePackages = {"org.createnet.raptor.broker"},
        exclude = {ErrorMvcAutoConfiguration.class, ArtemisAutoConfiguration.class}
)
public class Application extends BaseApplication {

    public static void main(String[] args) {
        Class clazz = Application.class;
        createInstance(clazz)
                .web(false)
                .run(buildArgs(clazz, args));
    }

    @Bean
    ApplicationStartup applicationStartup() {
        return new ApplicationStartup();
    }
    
    @Bean
    Broker broker() {
        return new Broker(getConfiguration());
    }

}
