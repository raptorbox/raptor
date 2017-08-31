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

package org.createnet.raptor.auth.events;

import org.createnet.raptor.common.dispatcher.events.UserApplicationEvent;
import org.createnet.raptor.events.Event;
import org.createnet.raptor.events.type.UserEvent;
import org.createnet.raptor.models.auth.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class UserEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void notify(User user, Event.EventType type) {
        UserEvent userEvent = new UserEvent(user);
        userEvent.setEvent(type.name());
        UserApplicationEvent ev = new UserApplicationEvent(this, userEvent);
        applicationEventPublisher.publishEvent(ev);
    }
    
    public void create(User user) {
        notify(user, Event.EventType.create);
    }
    
    public void update(User user) {
        notify(user, Event.EventType.update);
    }
    
    public void delete(User user) {
        notify(user, Event.EventType.delete);
    }

}
