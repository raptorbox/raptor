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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.createnet.raptor.models.objects.serializer.StreamSerializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Transient;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@JsonSerialize(using = StreamSerializer.class)
public class Stream extends StreamContainer {

    static final long serialVersionUID = 1000000000000105L;
    
    @JsonIgnore
    @Transient
    private final Logger logger = LoggerFactory.getLogger(Stream.class);

    protected String name;
    protected String type;
    protected String description;
    protected boolean dynamic = false;
    
    final protected Map<String, Channel> channels = new HashMap();

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
     *
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
     *
     * @param name
     * @param type
     * @return
     */
    public Stream addChannel(String name, String type) {
        return addChannel(name, type, null);
    }

    /**
     * Add a Channel to the stream
     *
     * @param channel
     * @return
     */
    public Stream addChannel(final Channel channel) {

        // skip if it exists
        final Channel prevChannel = this.channels.get(channel.name);
        if (prevChannel != null && prevChannel.type.equals(channel.type)) {
            return this;
        }

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

        if (json.has("dynamic")) {
            dynamic = json.get("dynamic").asBoolean(false);
        }
        
        if (!dynamic && !json.has("channels")) {
                parseChannels(json);            
                return;
        }

        if (json.has("name") && !json.get("name").asText().isEmpty()) {
            name = json.get("name").asText();
        }

        if (json.has("type")) {
            type = json.get("type").asText();
        }

        if (json.has("description")) {
            description = json.get("description").asText();
        }

        if (!dynamic && json.has("channels")) {
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

        if (!this.channels.isEmpty()) {
            for (Map.Entry<String, Channel> item : this.channels.entrySet()) {
                item.getValue().validate();
            }
        }

    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public String description() {
        return description;
    }

    public Map<String, Channel> channels() {
        return channels;
    }

    public boolean isDynamic() {
        return this.dynamic;
    }
    
    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }
    
}
