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
package org.createnet.raptor.models.objects.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.Stream;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class DeviceDeserializer extends JsonDeserializer<Device> {

    protected String getText(String fieldName, JsonNode tree) {
        if (tree.has(fieldName) && !tree.get(fieldName).isNull()) {
            return tree.get(fieldName).asText();
        }
        return null;
    }

    @Override
    public Device deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

        Device device = new Device();

        JsonNode tree = jp.getCodec().readTree(jp);

        if (tree.has("id")) {
            device.id(getText("id", tree));
        }

        if (tree.has("userId")) {
            device.userId(getText("userId", tree));
        }

        if (tree.has("name")) {
            device.name(getText("name", tree));
        }

        if (tree.has("description")) {
            device.description(getText("description", tree));
        }
        
        if (tree.has("domain")) {
            device.domain(getText("domain", tree));
        }

        if (tree.has("properties") && tree.get("properties").size() > 0) {
            device.properties().putAll(Device.getMapper().convertValue(tree.get("properties"), new TypeReference<Map<String, Object>>() {}));
        }

        if (tree.has("streams")) {

            if (tree.get("streams").isObject()) {

                Iterator<String> fieldNames = tree.get("streams").fieldNames();
                while (fieldNames.hasNext()) {

                    String name = fieldNames.next();
                    JsonNode jsonStream = tree.get("streams").get(name);
                    
                    if (name.isEmpty()) {
                        if(jsonStream.has("name")) {
                            name = jsonStream.get("name").asText();
                        }
                    }
                    
                    if(name.isEmpty()) {
                        continue;
                    }
                    
                    Stream stream = new Stream(name, jsonStream, device);
                    device.streams().put(stream.name(), stream);
                }
            }

            if (tree.get("streams").isArray()) {
                for (JsonNode jsonStream : tree.get("streams")) {
                    Stream stream = new Stream(jsonStream, device);
                    device.streams().put(stream.name(), stream);
                }
            }

        }

        if (tree.has("actions")) {

            if (tree.get("actions").isArray()) {
                for (JsonNode json : tree.get("actions")) {
                    Action actuation = new Action(json, device);
                    device.actions().put(actuation.name(), actuation);
                }
            }

            if (tree.get("actions").isObject()) {
                Iterator<String> fieldNames = tree.get("actions").fieldNames();
                while (fieldNames.hasNext()) {

                    String name = fieldNames.next();
                    JsonNode json = tree.get("actions").get(name);
                    
                    if (name.isEmpty()) {
                        if (json.has("name")) {
                            name = json.get("name").asText();
                        }
                    }
                    
                    if(name.isEmpty()) {
                        continue;
                    }
                    
                    Action actuation = new Action(name, json, device);
                    device.actions().put(actuation.name(), actuation);
                }
            }

        }

        return device;
    }
}
