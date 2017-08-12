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

package org.createnet.raptor.standalone;


import java.util.ArrayList;
import java.util.List;
import org.createnet.raptor.common.dispatcher.RaptorMessageHandler;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageHeaders;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class MultipleMessageHandler implements RaptorMessageHandler {

    final Logger log = LoggerFactory.getLogger(MultipleMessageHandler.class);
    
    final private List<RaptorMessageHandler> handlers = new ArrayList();
    
    public void addHandler(RaptorMessageHandler h) {
        handlers.add(h);
    }
    
    @Override
    public void handle(final DispatcherPayload dispatcherPayload, final MessageHeaders headers) {
        handlers.parallelStream().forEach((h) -> h.handle(dispatcherPayload, headers));
    }

}
