/*
 * Copyright 2016 CREATE-NET http://create-net.org
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
package org.createnet.raptor.http.events;

import org.createnet.raptor.http.service.EventEmitterService;


/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class AbstractEvent extends org.createnet.raptor.events.AbstractEvent {
  
  private String event;
  
  public void setEvent(EventEmitterService.EventName event) {
    setEvent(event.toString());
  }
  
}
