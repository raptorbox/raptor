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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.createnet.raptor.models.objects.deserializer.DeviceDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Luca Capra <luca.capra@gmail.com>
 */
@JsonDeserialize(using = DeviceDeserializer.class)
@Document
public class Device extends DeviceContainer {

    Logger logger = LoggerFactory.getLogger(Device.class);

    @JsonIgnore
    private boolean isNew = true;
    
    @Id
    public String id = Device.generateUUID();
    public String userId;
    public String parentId;
    public String path;

    public String name;
    public String description = "";

    public Long createdAt = Instant.now().getEpochSecond();
    public Long updatedAt = createdAt;

    final public Map<String, Object> customFields = new HashMap();
    final public Settings settings = new Settings();

    final public Map<String, Stream> streams = new HashMap();
    final public Map<String, Action> actions = new HashMap();

    /**
     * A serializable class containing the settings for a Device
     */
    static public class Settings {

        public boolean storeData = true;
        public boolean eventsEnabled = true;

        public boolean storeEnabled() {
            return storeData;
        }

        public boolean eventsEnabled() {
            return eventsEnabled;
        }
    }

    /**
     * Generates a UUID v4
     *
     * @return the UUID v4 as string
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public Device() {
    }

    public Device(String soid) {
        this.id = soid;
    }

    @Override
    public String toString() {
        return "Device<" + (this.id != null ? this.id : this.name) + ">";
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getParentId() {
        return parentId;
    }

    public String path() {
        return path;
    }

    @JsonIgnore
    public boolean isRoot() {
        return parentId == null;
    }

    /**
     * Set the current time in seconds
     */
    public void setUpdateTime() {
        updatedAt = Instant.now().getEpochSecond();
    }

    @Override
    public void validate() {

        if (this.name == null) {
            throw new ValidationException("name field missing");
        }

        if (!this.isNew() && this.id == null) {
            throw new ValidationException("id field missing");
        }

        if (!this.getStreams().isEmpty()) {
            this.getStreams().entrySet().forEach((item) -> {
                item.getValue().validate();
            });
        }

        if (!this.getActions().isEmpty()) {
            this.getActions().entrySet().forEach((item) -> {
                item.getValue().validate();
            });
        }

    }

    /**
     * Merge an device representation to the current instance
     *
     * @param json the JsonNode device representation
     */
    public void parse(JsonNode json) {
        parse(json.toString());
    }

    /**
     * Merge an device to the current instance
     *
     * @param device the device to merge from
     */
    public void parse(Device device) {

        id = device.id;
        userId = device.userId;
        parentId = device.parentId;
        path = device.path;

        name = device.name;
        description = device.description;

        createdAt = device.createdAt;
        updatedAt = device.updatedAt;

        customFields.clear();
        customFields.putAll(device.customFields);

        getStreams().clear();
        device.getStreams().entrySet().stream().forEach((el) -> {
            el.getValue().setDevice(this);
            getStreams().put(el.getKey(), el.getValue());
        });

        getActions().clear();
        device.getActions().entrySet().stream().forEach((el) -> {
            el.getValue().setDevice(this);
            getActions().put(el.getKey(), el.getValue());
        });

        isNew = (id == null);
    }

    @Override
    public void parse(String json) {
        try {
            parse(mapper.readValue(json, Device.class));
        } catch (IOException ex) {
            throw new ParserException(ex);
        }
    }

    /**
     * Map a JSON string properties to a Device instance
     *
     * @param json the json data
     * @return the Device instance
     */
    public static Device fromJSON(String json) {
        try {
            return mapper.readValue(json, Device.class);
        } catch (IOException e) {
            throw new RaptorComponent.ParserException(e);
        }
    }

    /**
     * Map JsonNode properties to a Device instance
     *
     * @param json the JsonNode data
     * @return the Device instance
     */
    public static Device fromJSON(JsonNode json) {
        return mapper.convertValue(json, Device.class);
    }

    @JsonIgnore
    public boolean isNew() {
        return isNew;
    }

    /**
     * Return the JsonNode representing the device
     *
     * @return the JsonNode representing the device
     */
    public ObjectNode toJsonNode() {
        return getMapper().valueToTree(this);
    }

    /**
     * Return the JSON string representing the device
     *
     * @return the JSON representation
     */
    public String toJSON() {
        try {
            String json = getMapper().writeValueAsString(this);
            return json;
        } catch (JsonProcessingException ex) {
            throw new ParserException(ex);
        }
    }

    /**
     * Return the Stream list
     *
     * @return a Map of Stream instances
     */
    public Map<String, Stream> getStreams() {
        return this.streams;
    }

    /**
     * Return a Stream by name
     *
     * @param name the name of the stream
     * @return a Stream instance
     */
    public Stream getStream(String name) {
        return getStreams().getOrDefault(name, null);
    }

    /**
     * Return the Action list
     *
     * @return a Map of Action instances
     */
    public Map<String, Action> getActions() {
        return this.actions;
    }

    /**
     * Return an Action
     *
     * @param name the name of the Action
     * @return a Map of Action instances
     */
    public Action getAction(String name) {
        return getActions().getOrDefault(name, null);
    }

    /**
     * Add a list of streams to the device, trying to merge channels if they exists already
     *
     * @param streams list of streams
     * @return
     */
    public Device addStreams(Collection<Stream> streams) {

        for (Stream stream : streams) {

            final Stream prevStream = this.streams.get(stream.name);

            for (Channel channel : stream.channels.values()) {
                // ensure device ref
                channel.setDevice(this);

                // merging into previous stream
                if (prevStream != null) {
                    prevStream.channels.put(channel.name, channel);
                }
            }

            // add new stream
            if (prevStream == null) {
                stream.setDevice(this);
                this.streams.put(stream.name, stream);
            } else {
                // copy details
                prevStream.type = stream.type;
                prevStream.description = stream.description;
            }

        }

        return this;
    }

    /**
     * Add a Stream in the device if it does not exists
     *
     * @param name
     * @return
     */
    public Stream addStream(String name) {

        Stream prevStream = getStream(name);
        if (prevStream != null) {
            return prevStream;
        }

        Stream stream = Stream.create(name);
        addStreams(Arrays.asList(stream));
        
        return stream;
    }

    /**
     * Add a channel to an stream, creating it if not available
     *
     * @param name
     * @param channelName
     * @param channelType
     * @return
     */
    public Stream addStream(String name, String channelName, String channelType) {
        Stream stream = addStream(name);
        stream.addChannel(channelName, channelType);
        stream.validate();
        return stream;
    }

    /**
     * Add a channel of the same name of the stream
     *
     * @param name
     * @param channelType
     * @return
     */
    public Stream addStream(String name, String channelType) {
        return addStream(name, name, channelType);
    }

    /**
     * Add a list of actions to the device
     *
     * @param values list of actions
     * @return
     */
    public Device addActions(Collection<Action> values) {
        values.stream().forEach((action) -> {
            action.setDevice(this);
            this.actions.put(action.name, action);
        });
        return this;
    }

    /**
     * Add an action to the device
     *
     * @param name
     * @return
     */
    public Action addAction(String name) {
        Action action = Action.create(name);
        addActions(Arrays.asList(action));
        return action;
    }

    /**
     * Return the current Device instance
     *
     * @return self-instance
     */
    @Override
    public Device getDevice() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Device) {
            Device sobj = (Device) obj;
            if (this.id != null && sobj.id != null) {
                return sobj.id.equals(this.id);
            }
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.id);
        return hash;
    }

}
