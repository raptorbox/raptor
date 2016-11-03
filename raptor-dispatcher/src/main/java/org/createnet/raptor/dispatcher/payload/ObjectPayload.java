/*
 * Copyright 2016 Luca Capra <luca.capra@create-net.org>.
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
package org.createnet.raptor.dispatcher.payload;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;

/**
 *
 * @author Luca Capra <luca.capra@create-net.org>
 */
public class ObjectPayload extends AbstractPayload {

    final public String userId;
    final public JsonNode object;
    final public String path;

    public ObjectPayload(ServiceObject obj, String op) {
        userId = obj.userId;
        object = obj.toJsonNode();
        path = obj.path();
        type = MessageType.object.name();
        this.op = op;
    }

    @Override
    public String toString() {
        try {
            return ServiceObject.getMapper().writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            throw new RaptorComponent.ParserException(ex);
        }
    }

}
