/*
 * Copyright 2016 Luca Capra <lcapra@create-net.org>.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ActionStatus {

    public String id;
    public String status;
    public int createdAt;

    public String actionId;
    public String objectId;

    public ActionStatus() {
    }

    public ActionStatus(Action action, String status) {
        this(status);
        this.actionId = action.name;
        this.objectId = action.getServiceObject().id;
    }

    public ActionStatus(String status) {
        this.status = status;
        this.createdAt = (int) (System.currentTimeMillis() / 1000);
        this.id = UUID.randomUUID().toString();
    }

    private final static ObjectMapper mapper = new ObjectMapper();

    private ObjectMapper getMapper() {
        return mapper;
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
            return ServiceObject.getMapper().readValue(rawStatus, ActionStatus.class);
        } catch (IOException ex) {
            throw new RaptorComponent.ParserException(ex);
        }
    }

    public static ActionStatus parseJSON(JsonNode rawStatus) {
        return ServiceObject.getMapper().convertValue(rawStatus, ActionStatus.class);
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
