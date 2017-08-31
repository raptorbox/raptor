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
package org.createnet.raptor.common.dispatcher.events;

import org.createnet.raptor.events.type.UserEvent;
import org.springframework.context.ApplicationEvent;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class UserApplicationEvent extends ApplicationEvent {

    private final UserEvent userEvent;
    
    public UserApplicationEvent(Object source, UserEvent dev) {
        super(source);
        this.userEvent = dev;
    }

    public UserEvent getUserEvent() {
        return userEvent;
    }
    
}
