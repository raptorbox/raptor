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
package org.createnet.raptor.application;

import org.createnet.raptor.common.dispatcher.events.AppApplicationEvent;
import org.createnet.raptor.events.Event;
import org.createnet.raptor.events.type.AppEvent;
import org.createnet.raptor.models.app.App;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class AppEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void notify(App app, Event.EventType type) {

        AppEvent appEvent = new AppEvent(app);
        appEvent.setEvent(type.name());
        AppApplicationEvent ev = new AppApplicationEvent(this, appEvent);
        applicationEventPublisher.publishEvent(ev);
    }

    public void create(App app) {
        notify(app, Event.EventType.create);
    }

    public void update(App app) {
        notify(app, Event.EventType.update);
    }

    public void delete(App app) {
        notify(app, Event.EventType.delete);
    }

}
