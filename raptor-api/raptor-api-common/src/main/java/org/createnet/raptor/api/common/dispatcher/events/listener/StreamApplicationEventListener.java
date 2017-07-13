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
package org.createnet.raptor.api.common.dispatcher.events.listener;

import org.createnet.raptor.api.common.dispatcher.DispatcherService;
import org.createnet.raptor.api.common.dispatcher.events.StreamApplicationEvent;
import org.createnet.raptor.events.type.DataEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Component
public class StreamApplicationEventListener implements ApplicationListener<StreamApplicationEvent> {

    @Autowired
    DispatcherService dispatcher;

    @Override
    public void onApplicationEvent(StreamApplicationEvent event) {
        
        DataEvent streamEvent = event.getDataEvent();
        
        if (!streamEvent.getDevice().settings().eventsEnabled()) {
            return;
        }

        dispatcher.notifyDataEvent(streamEvent.getStream(), streamEvent.getRecord());
    }

}
