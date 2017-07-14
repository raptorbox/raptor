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
package org.createnet.raptor.api.common.configuration;

import org.createnet.raptor.api.common.dispatcher.events.listener.ActionApplicationEventListener;
import org.createnet.raptor.api.common.dispatcher.events.listener.DeviceApplicationEventListener;
import org.createnet.raptor.api.common.dispatcher.events.listener.StreamApplicationEventListener;
import org.createnet.raptor.api.common.dispatcher.events.listener.TreeNodeApplicationEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Configuration
public class EventListenerConfiguration {
    
    @Bean
    DeviceApplicationEventListener deviceApplicationEventListener() {
        return new DeviceApplicationEventListener();
    }
    
    @Bean
    StreamApplicationEventListener streamApplicationEventListener() {
        return new StreamApplicationEventListener();
    }
    
    @Bean
    ActionApplicationEventListener actionApplicationEventListener() {
        return new ActionApplicationEventListener();
    }
    
    @Bean
    TreeNodeApplicationEventListener treeNodeApplicationEventListener() {
        return new TreeNodeApplicationEventListener();
    }
    
    
}
