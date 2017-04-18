/*
 * Copyright 2017 Luca Capra <luca.capra@fbk.eu>.
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
package org.createnet.raptor.models.payload;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.validation.Payload;
import org.createnet.raptor.models.exception.PayloadParserException;
import org.createnet.raptor.models.objects.RaptorContainer;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public interface DispatcherPayload {

    public enum MessageType {
        object, stream, action, data, 
        user, permission, role
    }
    
    /**
     * Return the type of message
     * 
     * @return type of message
     */
    public MessageType getType();
    
    /**
     * Return the operation label
     * 
     * @return label of the operation
     */    
    public String getOp();
    
    @Override
    public String toString();
    
    public static DispatcherPayload parseJSON(String json) {
        try {
            return DispatcherPayload.parseJSON(RaptorContainer.getMapper().readValue(json, JsonNode.class));
        } catch (IOException ex) {
            throw new PayloadParserException(ex);
        }
    }
    
    /**
     * Parse a json node an return the correct DispatcherPayload implementation
     * 
     * @param json
     * @return 
     */
    public static DispatcherPayload parseJSON(JsonNode json) {

        if(!json.has("type")) {
            throw new PayloadParserException("Field `type` is missing");
        }

        try {
            
            String type = json.get("type").asText();
            switch(MessageType.valueOf(type)) {
                case action:
                    return RaptorContainer.getMapper().treeToValue(json, ActionPayload.class);
                case data:
                    return RaptorContainer.getMapper().treeToValue(json, DataPayload.class);
                case object:
                    return RaptorContainer.getMapper().treeToValue(json, DevicePayload.class);
                case stream:
                    return RaptorContainer.getMapper().treeToValue(json, StreamPayload.class);                    
//                case permission:
//                    return RaptorContainer.getMapper().treeToValue(json, PermissionPayload.class);
//                case role:
//                    return RaptorContainer.getMapper().treeToValue(json, RolePayload.class);
//                case user:
//                    return RaptorContainer.getMapper().treeToValue(json, UserPayload.class);
                default:
                    throw new Exception("Field `type` does not match a known payload: " + type);
            }
            
        } catch (Exception ex) {
            throw new PayloadParserException(ex);
        }
    }
    
}