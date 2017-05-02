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
package org.createnet.raptor.models.objects;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.createnet.raptor.models.objects.RaptorComponent.ParserException;
import org.createnet.raptor.models.objects.RaptorComponent.ValidationException;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class Channel extends StreamContainer {

    public static final List<String> Types = new ArrayList(Arrays.asList(
            "number",
            "string",
            "boolean"
    ));

    public String name;
    public String type;
    public String unit;

    public static Channel create(String name, String type, String unit) {
        Channel channel = new Channel();
        channel.name = name;
        channel.type = type;
        channel.unit = unit;

        return channel;
    }

    public static Channel create(String name, String type) {
        return Channel.create(name, type, null);
    }

    public Channel() {
    }

    public Channel(JsonNode json) {
        this();
        parse(json);
    }

    public Channel(String name, JsonNode json) {
        this();
        this.name = name;
        parse(json);
    }

    @Override
    public void validate() {

        if (name == null) {
            throw new ValidationException("Channel name is empty");
        }

        if (type == null) {
            throw new ValidationException("Channel type is empty");
        }

        if (!Types.contains(type.toLowerCase())) {
            throw new ValidationException("Channel type not supported: " + type);
        }

    }

    @Override
    public void parse(String json) {
        try {
            parse(mapper.readTree(json));
        } catch (IOException ex) {
            throw new ParserException(ex);
        }
    }

    public void parse(JsonNode json) {

        if (json.isTextual()) {
            type = json.asText();
        } else {
            if (json.has("name")) {
                name = json.get("name").asText();
            }

            if (json.has("type")) {
                type = json.get("type").asText();
            }

            if (json.has("unit")) {
                unit = json.get("unit").asText();
            }
        }
    }

}
