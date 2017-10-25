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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.createnet.raptor.models.acl.EntityType;
import org.createnet.raptor.models.acl.Operation;
import org.createnet.raptor.models.exception.PayloadParserException;
import org.createnet.raptor.models.objects.RaptorContainer;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public interface DispatcherPayload {

    /**
     * Return the type of message
     * 
     * @return type of message
     */
    public EntityType getType();
    
    /**
     * Return the operation label
     * 
     * @return label of the operation
     */    
    public Operation getOp();
    
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
            switch(EntityType.valueOf(type)) {
                case action:
                    return RaptorContainer.getMapper().treeToValue(json, ActionPayload.class);
                case data:
                    return RaptorContainer.getMapper().treeToValue(json, DataPayload.class);
                case device:
                    return RaptorContainer.getMapper().treeToValue(json, DevicePayload.class);
                case stream:
                    return RaptorContainer.getMapper().treeToValue(json, StreamPayload.class);                    
//                case permission:
//                    return RaptorContainer.getMapper().treeToValue(json, PermissionPayload.class);
//                case role:
//                    return RaptorContainer.getMapper().treeToValue(json, RolePayload.class);
                case user:
                    return RaptorContainer.getMapper().treeToValue(json, UserPayload.class);
                case token:
                    return RaptorContainer.getMapper().treeToValue(json, TokenPayload.class);
                default:
                    throw new Exception("Field `type` does not match a known payload: " + type);
            }
            
        } catch (Exception ex) {
            throw new PayloadParserException(ex);
        }
    }
    
}
