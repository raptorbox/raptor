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
package org.createnet.raptor.inventory;

import org.createnet.raptor.api.common.events.DeviceApplicationEvent;
import org.createnet.raptor.events.Event;
import org.createnet.raptor.events.type.DeviceEvent;
import org.createnet.raptor.models.objects.Device;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class DeviceEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void notify(Device dev, Event.EventType type) {
        
        DeviceEvent deviceEvent = new DeviceEvent(dev);
        deviceEvent.setEvent(type.name());
        DeviceApplicationEvent ev = new DeviceApplicationEvent(this, deviceEvent);
        applicationEventPublisher.publishEvent(ev);
    }
    
    public void create(Device dev) {
        notify(dev, Event.EventType.create);
    }
    
    public void update(Device dev) {
        notify(dev, Event.EventType.update);
    }
    
    public void delete(Device dev) {
        notify(dev, Event.EventType.delete);
    }

}
