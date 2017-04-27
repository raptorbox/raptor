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
package org.createnet.raptor.auth.service.services;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.createnet.raptor.api.common.authentication.RaptorUserDetails;
import org.createnet.raptor.models.auth.request.SyncRequest;
import static org.createnet.raptor.auth.service.Application.mapper;
import org.createnet.raptor.models.payload.ActionPayload;
import org.createnet.raptor.models.payload.StreamPayload;
import org.createnet.raptor.models.auth.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
@Service
public class BrokerMessageHandler {
    
    final Logger logger = LoggerFactory.getLogger(BrokerMessageHandler.class);
    
    @Autowired
    private UserService userService;

    @Autowired
    private DeviceService deviceService;

    public void handle(Message<?> message) {
        
        logger.debug("MQTT message received");
        
        JsonNode json;
        try {
            json = mapper.readTree(message.getPayload().toString());
        } catch (IOException ex) {
            throw new MessagingException("Cannot convert JSON ["+ ex.getMessage() +"] payload:" + message.getPayload().toString());
        }

        switch (json.get("type").asText()) {
            case "object":                
                                
                User user = userService.getByUuid(json.get("userId").asText());
                UserDetails details = new RaptorUserDetails(user);
                final Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), details.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                SyncRequest req = new SyncRequest();
                req.userId = user.getUuid();
                req.operation = json.get("op").asText();
                req.objectId = json.get("object").get("id").asText();
                req.created = json.get("object").get("createdAt").asLong();

                logger.debug("MQTT object message op:{} id:{}", req.operation, req.objectId);
                deviceService.sync(user, req);

                SecurityContextHolder.getContext().setAuthentication(null);

                break;
            case "data":
                StreamPayload data = mapper.convertValue(json, StreamPayload.class);
                logger.debug("MQTT data message raw:{}", json);
                break;
            case "action":
                ActionPayload action = mapper.convertValue(json, ActionPayload.class);
                logger.debug("MQTT action message raw:{}", json);
                break;
            case "user":
                // TODO: Add event notification in auth service
                break;
        }

    }

}
