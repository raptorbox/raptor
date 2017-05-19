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
package org.createnet.raptor.data;

import org.createnet.raptor.api.common.dispatcher.events.StreamApplicationEvent;
import org.createnet.raptor.events.Event;
import org.createnet.raptor.events.type.DataEvent;
import org.createnet.raptor.models.data.RecordSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class StreamEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void notify(RecordSet record, Event.EventType type) {
        
        DataEvent dataEvent = new DataEvent(record.getStream(), record);
        dataEvent.setEvent(type.name());
        
        StreamApplicationEvent ev = new StreamApplicationEvent(this, dataEvent);
        applicationEventPublisher.publishEvent(ev);
    }
    
    public void push(RecordSet record) {
        notify(record, Event.EventType.push);
    }
    
    public void pull(RecordSet record) {
        notify(record, Event.EventType.pull);
    }
    
}
