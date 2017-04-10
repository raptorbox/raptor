/*
 * Copyright 2017  FBK/CREATE-NET <http://create-net.fbk.eu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.createnet.raptor.models.objects;

import org.createnet.raptor.models.objects.serializer.StreamSerializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@JsonSerialize(using = StreamSerializer.class)
public class Stream extends StreamContainer {

    Logger logger = LoggerFactory.getLogger(Stream.class);

    public String name;
    public String type;
    public String description;
    
    public static Stream create(String name, String type, String description) {
        Stream s = new Stream();
        s.name = name;
        s.type = type;
        s.description = description;
        return s;
    }
    
    public static Stream create(String name, String type) {
        return create(name, type, null);
    }
    
    public static Stream create(String name) {
        return create(name, null, null);
    }
        
    final public Map<String, Channel> channels = new HashMap();

    public Stream(String json, Device object) {

        initialize();
        try {
            JsonNode tree = mapper.readTree(json);
            parse(tree, object);
        } catch (IOException ex) {
            throw new ParserException(ex);
        }
    }

    public Stream(JsonNode json, Device object) {
        initialize();
        parse(json, object);
    }

    public Stream(String name, JsonNode json, Device object) {
        initialize();
        this.name = name;
        parse(json, object);
    }

    public Stream(String json) {
        initialize();
        try {
            JsonNode tree = mapper.readTree(json);
            parse(tree, null);
        } catch (IOException ex) {
            throw new ParserException(ex);
        }
    }

    public Stream(JsonNode json) {
        initialize();
        parse(json, null);
    }

    public Stream() {
        initialize();
    }

    protected void initialize() {
    }

    /**
     * Add a Channel to the stream
     * @param name
     * @param type
     * @param unit
     * @return
     */
    public Stream addChannel(String name, String type, String unit) {
        Channel channel = Channel.create(name, type, unit);
        return addChannel(channel);
    }

    /**
     * Add a Channel to the stream
     * @param name
     * @param type
     * @return
     */
    public Stream addChannel(String name, String type) {
        return addChannel(name, type, null);
    }
    
    /**
     * Add a Channel to the stream
     * @param channel
     * @return
     */
    public Stream addChannel(Channel channel) {
        channel.setContainer(this);
        this.channels.put(channel.name, channel);
        return this;
    }
    
    protected void parse(JsonNode json, Device object) {
        this.setDevice(object);
        parse(json);
    }

    protected void parseChannels(JsonNode jsonChannels) {

        if (jsonChannels.isObject()) {

            Iterator<String> fieldNames = jsonChannels.fieldNames();
            while (fieldNames.hasNext()) {

                String channelName = fieldNames.next();
                JsonNode jsonChannel = jsonChannels.get(channelName);

                Channel channel = new Channel(channelName, jsonChannel);
                channel.setStream(this);
                channels.put(channel.name, channel);
            }
        }

        if (jsonChannels.isArray()) {
            for (JsonNode jsonChannel : jsonChannels) {
                Channel channel = new Channel(jsonChannel);
                channel.setStream(this);
                channels.put(channel.name, channel);
            }
        }
    }

    protected void parse(JsonNode json) {

        if (!json.has("channels")) {
            parseChannels(json);
            return;
        }

        if (json.has("name")) {
            name = json.get("name").asText();
        }

        if (json.has("type")) {
            type = json.get("type").asText();
        }

        if (json.has("description")) {
            description = json.get("description").asText();
        }

        if (json.has("channels")) {
            parseChannels(json.get("channels"));
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

    @Override
    public void validate() {
        if (this.name == null || this.name.isEmpty()) {
            throw new ValidationException("Stream name is required");
        }
        if (this.channels.isEmpty()) {
            throw new ValidationException("Stream must have at least a channel");
        } else {
            for (Map.Entry<String, Channel> item : this.channels.entrySet()) {
                item.getValue().validate();
            }
        }
    }

}
