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
package org.createnet.raptor.auth.services;

import org.createnet.raptor.common.dispatcher.RaptorMessageHandler;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
@Component
public class AuthMessageHandler implements RaptorMessageHandler {
    
    final Logger logger = LoggerFactory.getLogger(AuthMessageHandler.class);
    
    @Autowired
    private UserService userService;

    @Autowired
    private AuthDeviceService deviceService;

    @Override
    public void handle(DispatcherPayload dispatcherPayload, MessageHeaders headers) {    
        
        return;
        
//        switch (dispatcherPayload.getType()) {
//            case device:
//
//                DevicePayload payload = (DevicePayload) dispatcherPayload;
//                User user = userService.getByUuid(payload.getUserId());
//                UserDetails details = new RaptorUserDetails(user);
//                
//                // Login is required by spring ACL services implementation
//                final Authentication authentication = new LoginAuthenticationToken(user, null, details.getAuthorities());
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//                
//                SyncRequest req = new SyncRequest();
//                req.userId = user.getUuid();
//                req.operation = payload.getOp();
//                req.objectId = payload.getDevice().id();
//                req.created = payload.getDevice().createdAt().getEpochSecond();
//
//                logger.debug("MQTT device message op:{} id:{}", req.operation, req.objectId);
//                deviceService.sync(user, req);
//
//                SecurityContextHolder.getContext().setAuthentication(null);
//
//                break;
////            case data:
////                StreamPayload data = mapper.convertValue(json, StreamPayload.class);
////                logger.debug("MQTT data message raw:{}", json);
////                break;
////            case action:
////                ActionPayload action = mapper.convertValue(json, ActionPayload.class);
////                logger.debug("MQTT action message raw:{}", json);
////                break;
////            case user:
////                // TODO: Add event notification in auth service
////                break;
//        }

    }


}
