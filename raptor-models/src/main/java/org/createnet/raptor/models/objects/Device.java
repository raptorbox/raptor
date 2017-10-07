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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.createnet.raptor.models.objects.deserializer.DeviceDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.querydsl.core.annotations.QueryEntity;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Luca Capra <luca.capra@gmail.com>
 */
@JsonDeserialize(using = DeviceDeserializer.class)
@Document
@QueryEntity
public class Device extends DeviceContainer {
    
    static final long serialVersionUID = 1000000000000051L;
    
    @JsonIgnore
    @Transient
    private final Logger logger = LoggerFactory.getLogger(Device.class);

    @JsonIgnore
    @Transient
    private boolean isNew = true;

    @Id
    private String id = generateUUID();

    @Indexed
    private String userId;

    @Indexed
    private String name;

    @Indexed
    private String description = "";

    @Indexed
    private Long createdAt = Instant.now().getEpochSecond();

    @Indexed
    private Long updatedAt = createdAt;

    @Indexed
    final private Map<String, Object> properties = new HashMap();

    final private Settings settings = new Settings();
    final private Map<String, Stream> streams = new HashMap();
    final private Map<String, Action> actions = new HashMap();

    /**
     * A serializable class containing the settings for a Device
     */
    static public class Settings implements Serializable {
        
        static final long serialVersionUID = 1000000000000055L;
        
        public boolean storeData = true;
        public boolean eventsEnabled = true;

        public Settings() {
        }

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
        return "Device<" + (id != null ? id : "no id") + "> " + (name != null ? name : "no name");
    }

    /**
     * Merge a Device instance in the current one, overriding part of its fields
     * Differs from parse as it will keep previous fields state, like stream
     * list or properties
     *
     * @param raw
     * @return
     */
    public Device merge(Device raw) {

        this.name(raw.name());
        this.description(raw.description());

        this.settings().eventsEnabled = raw.settings().eventsEnabled;
        this.settings().storeData = raw.settings().storeData;

        this.properties().putAll(raw.properties());

        this.streams().putAll(raw.streams());
//		for (Iterator iterator = this.streams().entrySet().iterator(); iterator.hasNext();) {
//			Entry<String, Stream> entry = (Entry<String, Stream>) iterator.next();
//			String value = entry.getKey();
//			if (!raw.streams().containsKey(value)) {
//				iterator.remove(); 
//			}
//		}

        // update device internal ref
        this.streams().entrySet().stream().forEach((el) -> {
            el.getValue().setDevice(this);
        });

        this.actions().putAll(raw.actions());
        // update device internal ref
        this.actions().entrySet().stream().forEach((el) -> {
            el.getValue().setDevice(this);
        });

        return this;
    }

    public void setDefaults() {
        this.id = Device.generateUUID();
        this.createdAt = Instant.now().getEpochSecond();
        this.updatedAt = createdAt;
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
        
        if (!this.isNew() && (this.id == null || this.id.isEmpty())) {
            throw new ValidationException("id field missing");
        }

        if (!this.streams().isEmpty()) {
            this.streams().entrySet().forEach((item) -> {
                item.getValue().validate();
            });
        }

        if (!this.actions().isEmpty()) {
            this.actions().entrySet().forEach((item) -> {
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
     * Substitute current device instance with provided one properties Differs
     * from merge as it will clear previous fields state
     *
     * @param device the device to merge from
     */
    public void parse(Device device) {

        id = device.id();
        userId = device.userId();

        name = device.name();
        description = device.description();

        createdAt = device.getCreatedAt();
        updatedAt = device.getUpdatedAt();

        settings.eventsEnabled = device.settings().eventsEnabled;
        settings.storeData = device.settings().storeData;

        properties.clear();
        properties.putAll(device.properties);

        streams().clear();
        device.streams().entrySet().stream().forEach((el) -> {
            el.getValue().setDevice(this);
            streams().put(el.getKey(), el.getValue());
        });

        actions().clear();
        device.actions().entrySet().stream().forEach((el) -> {
            el.getValue().setDevice(this);
            actions().put(el.getKey(), el.getValue());
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

    @JsonProperty
    public String name() {
        return name;
    }

    @JsonProperty
    public String description() {
        return description;
    }

    @JsonIgnore
    public Instant createdAt() {
        return Instant.ofEpochSecond(createdAt);
    }

    @JsonIgnore
    public Instant updatedAt() {
        return Instant.ofEpochSecond(updatedAt);
    }

    @JsonProperty
    public long getCreatedAt() {
        return createdAt;
    }

    @JsonProperty
    public long getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty
    public String id() {
        return id;
    }

    @JsonProperty
    public String userId() {
        return userId;
    }

    @JsonProperty
    public Map<String, Object> properties() {
        return properties;
    }

    @JsonProperty
    public Settings settings() {
        return settings;
    }

    /**
     * Return the Stream list
     *
     * @return a Map of Stream instances
     */
    @JsonProperty
    public Map<String, Stream> streams() {
        return this.streams;
    }

    /**
     * Return the Action list
     *
     * @return a Map of Action instances
     */
    @JsonProperty
    public Map<String, Action> actions() {
        return this.actions;
    }

    /**
     * Return a Stream by name
     *
     * @param name the name of the stream
     * @return a Stream instance
     */
    public Stream stream(String name) {
        return streams().getOrDefault(name, null);
    }

    /**
     * Return an Action
     *
     * @param name the name of the Action
     * @return a Map of Action instances
     */
    public Action action(String name) {
        return actions().getOrDefault(name, null);
    }

    public Device id(String id) {
        this.id = id;
        return this;
    }

    public Device userId(String uid) {
        this.userId = uid;
        return this;
    }

    public Device name(String name) {
        this.name = name;
        return this;
    }

    public Device description(String description) {
        this.description = description;
        return this;
    }

    public Device createdAt(Long createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Device updatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    @JsonIgnore
    public boolean isNew() {
        return isNew;
    }

    /**
     * Return the Stream list
     *
     * @return a Map of Stream instances
     */
    @Deprecated
    public Map<String, Stream> getStreams() {
        return this.streams;
    }

    /**
     * Return a Stream by name
     *
     * @param name the name of the stream
     * @return a Stream instance
     */
    @Deprecated
    public Stream getStream(String name) {
        return streams().getOrDefault(name, null);
    }

    /**
     * Return the Action list
     *
     * @return a Map of Action instances
     */
    @Deprecated
    public Map<String, Action> getActions() {
        return this.actions;
    }

    /**
     * Return an Action
     *
     * @param name the name of the Action
     * @return a Map of Action instances
     */
    @Deprecated
    public Action getAction(String name) {
        return actions().getOrDefault(name, null);
    }

    /**
     * Add a list of streams to the device, trying to merge channels if they
     * exists already
     *
     * @param streams list of streams
     * @return
     */
    public Device addStreams(Collection<Stream> streams) {

        for (Stream stream : streams) {

            final Stream prevStream = this.streams().get(stream.name);

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
                this.streams().put(stream.name, stream);
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

        Stream prevStream = stream(name);
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

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Settings getSettings() {
        return settings;
    }

}
