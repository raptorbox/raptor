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
package org.createnet.raptor.models.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.RaptorContainer;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionStatus {

    public String id;
    public String status;
    public int createdAt = (int) (Instant.now().toEpochMilli() / 1000);
    public String actionId;
    public String objectId;

    public ActionStatus() {
    }

    public ActionStatus(Action action, String status) {
        this(status);
        this.actionId = action.name;
        this.objectId = action.getDevice().id;
    }

    public ActionStatus(String status) {
        this.status = status;
        this.id = UUID.randomUUID().toString();
    }

    private final static ObjectMapper mapper = RaptorContainer.getMapper();

    private ObjectMapper getMapper() {
        return mapper;
    }

    /**
     * Set status
     *
     * @param s
     * @return
     */
    public ActionStatus status(String s) {
        this.status = s;
        return this;
    }
    
    /**
     * Get status
     *
     * @return
     */
    public String status() {
        return this.status;
    }

    public String toJSON() {
        try {
            return getMapper().writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            throw new RaptorComponent.ParserException(ex);
        }
    }

    public ObjectNode toJsonNode() {
        return getMapper().convertValue(this, ObjectNode.class);
    }

    public static ActionStatus parseJSON(String rawStatus) {
        try {
            return Device.getMapper().readValue(rawStatus, ActionStatus.class);
        } catch (IOException ex) {
            throw new RaptorComponent.ParserException(ex);
        }
    }

    public static ActionStatus parseJSON(JsonNode rawStatus) {
        return Device.getMapper().convertValue(rawStatus, ActionStatus.class);
    }

    @Override
    public String toString() {
        try {
            return toJSON();
        } catch (RaptorComponent.ParserException ex) {
            return null;
        }
    }

}
