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
package org.createnet.raptor.api.common.dispatcher;

import org.createnet.raptor.api.common.dispatcher.events.DeviceApplicationEvent;
import org.createnet.raptor.events.type.DeviceEvent;
import org.createnet.raptor.models.acl.Permissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Component
public class MqttDispatcherEventListener implements ApplicationListener<DeviceApplicationEvent> {

    @Autowired
    DispatcherService dispatcher;

    @Override
    public void onApplicationEvent(DeviceApplicationEvent event) {
        
        DeviceEvent objEvent = event.getDeviceEvent();
        
        if (!objEvent.getObject().settings().eventsEnabled()) {
            return;
        }

        dispatcher.notifyDeviceEvent(Permissions.valueOf(objEvent.getParentEvent()), objEvent.getObject());
    }
}
