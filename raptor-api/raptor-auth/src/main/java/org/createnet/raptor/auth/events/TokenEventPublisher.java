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

import org.createnet.raptor.common.dispatcher.events.TokenApplicationEvent;
import org.createnet.raptor.events.Event;
import org.createnet.raptor.events.type.TokenEvent;
import org.createnet.raptor.models.auth.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class TokenEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void notify(Token token, Event.EventType type) {
        TokenEvent tokenEvent = new TokenEvent(token);
        tokenEvent.setEvent(type.name());
        TokenApplicationEvent ev = new TokenApplicationEvent(this, tokenEvent);
        applicationEventPublisher.publishEvent(ev);
    }
    
    public void create(Token token) {
        notify(token, Event.EventType.create);
    }
    
    public void update(Token token) {
        notify(token, Event.EventType.update);
    }
    
    public void delete(Token token) {
        notify(token, Event.EventType.delete);
    }

}
