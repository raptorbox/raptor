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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.query.IQuery;
import org.createnet.raptor.models.query.MapQuery;
import org.createnet.raptor.models.query.StringListQuery;
import org.createnet.raptor.models.query.TextQuery;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 * @param <T>
 */
abstract public class AbstractQueryDeserializer<T extends IQuery> extends JsonDeserializer {

    protected void handleTextQuery(String key, TextQuery t, JsonNode json) {

        if (!json.has(key)) {
            return;
        }

        JsonNode node = json.get(key);
        if (node.isTextual()) {
            t.match(node.asText());
            return;
        }
        if (node.isArray()) {

            for (int i = 0; i < node.size(); i++) {
                t.in(node.get(i).asText());
            }

            return;
        }

        if (!node.isObject()) {
            return;
        }

        if (node.has("equals") && !node.get("equals").isNull()) {
            t.match(node.get("equals").asText());
        }

        if (node.has("contains") && !node.get("contains").isNull()) {
            t.contains(node.get("contains").asText());
        }

        if (node.has("startWith") && !node.get("startWith").isNull()) {
            t.startWith(node.get("startWith").asText());
        }

        if (node.has("endWith") && !node.get("endWith").isNull()) {
            t.endWith(node.get("endWith").asText());
        }

        if (node.has("in") && node.get("in").isArray() && !node.get("in").isNull()) {
            JsonNode arr = node.get("in");
            String[] s = new String[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                String v = arr.get(i).asText();
                s[i] = v;
            }
            t.in(s);
        }

    }

    protected void handleMapQuery(String key, MapQuery map, JsonNode json) {

        if (!json.has(key)) {
            return;
        }

        JsonNode node = json.get(key);

        if (node.has("containsKey") && !node.get("containsKey").isNull()) {
            map.containsKey(node.get("containsKey").asText());
        }

        if (node.has("containsValue") && !node.get("containsValue").isNull()) {
            map.containsValue(Device.getMapper().convertValue(node.get("containsValue"), Object.class));
        }

        if (node.has("has") && !node.get("has").isNull()) {

            Map<String, Object> hasMap = Device.getMapper().convertValue(node.get("has"), new TypeReference<Map<String, Object>>() {
            });

            map.has(hasMap);
        }

        // map an object without specific fields to "has"
        if (node.isObject() && !node.has("has") && !node.has("containsValue") && !node.has("containsKey")) {

            Map<String, Object> hasMap = Device.getMapper().convertValue(node, new TypeReference<Map<String, Object>>() {
            });

            map.has(hasMap);
        }

    }

    protected void handleStringListQuery(String key, StringListQuery map, JsonNode json) {

        if (!json.has(key)) {
            return;
        }

        JsonNode node = json.get(key);

        if (node.has("contains") && !node.get("contains").isNull()) {
            if (node.get("contains").isArray()) {
                for (JsonNode el : node.get("contains")) {
                    map.in(el.asText());
                }
            } else {
                map.in(node.get("containsValue").asText());
            }
        }

    }

    @Override
    abstract public T deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException;
}
