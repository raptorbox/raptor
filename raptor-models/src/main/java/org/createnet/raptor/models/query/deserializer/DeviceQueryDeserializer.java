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
package org.createnet.raptor.models.query.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.createnet.raptor.models.query.DeviceQuery;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class DeviceQueryDeserializer extends AbstractQueryDeserializer<DeviceQuery> {

    @Override
    public DeviceQuery deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        DeviceQuery query = new DeviceQuery();

        JsonNode node = jp.getCodec().readTree(jp);
        
        if(node.has("userId")) {
            query.userId(node.get("userId").asText());
        }
        
        handleTextQuery("name", query.name, node);
        handleTextQuery("description", query.description, node);
        handleTextQuery("domain", query.domain, node);
        handleTextQuery("id", query.id, node);

        handleMapQuery("properties", query.properties, node);

        return query;
    }

}
