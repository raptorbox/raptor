/*
 * Copyright 2016 CREATE-NET
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
import org.createnet.raptor.auth.entity.SyncRequest;
import static org.createnet.raptor.auth.service.Application.mapper;
import org.createnet.raptor.auth.service.RaptorUserDetailsService;
import org.createnet.raptor.dispatcher.payload.ActionPayload;
import org.createnet.raptor.dispatcher.payload.StreamPayload;
import org.createnet.raptor.models.auth.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <luca.capra@create-net.org>
 */
@Service
public class BrokerMessageHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private DeviceService deviceService;

    public void handle(Message<?> message) {

        System.out.println(message.getPayload());
        JsonNode json;
        try {
            json = mapper.readTree(message.getPayload().toString());
        } catch (IOException ex) {
            throw new MessagingException("Cannot convert JSON payload: " + message.getPayload().toString());
        }

        switch (json.get("type").asText()) {
            case "object":


                User user = userService.getByUuid(json.get("userId").asText());
                UserDetails details = new RaptorUserDetailsService.RaptorUserDetails(user);
                final Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), details.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                SyncRequest req = new SyncRequest();
                req.userId = user.getUuid();
                req.operation = json.get("op").asText();
                req.objectId = json.get("object").get("id").asText();
                req.created = json.get("object").get("createdAt").asLong();

                deviceService.sync(user, req);

                SecurityContextHolder.getContext().setAuthentication(null);
                break;
            case "data":
                StreamPayload data = mapper.convertValue(json, StreamPayload.class);
                break;
            case "action":
                ActionPayload action = mapper.convertValue(json, ActionPayload.class);
                break;
            case "user":
                // TODO: Add event notification in auth service
                break;
        }

    }

}
