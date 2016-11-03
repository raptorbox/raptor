/*
 * Copyright 2016  CREATE-NET <http://create-net.org>
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
import org.createnet.raptor.models.objects.deserializer.ServiceObjectDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luca Capra <luca.capra@gmail.com>
 */
@JsonDeserialize(using = ServiceObjectDeserializer.class)
public class ServiceObject extends ServiceObjectContainer {

    Logger logger = LoggerFactory.getLogger(ServiceObject.class);

    @JsonIgnore
    private boolean isNew = true;

    public String userId;
    public String id = ServiceObject.generateUUID();
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
     * A serializable class containing the settings for a ServiceObject
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

    public ServiceObject() {
    }

    public ServiceObject(String soid) {
        this.id = soid;
    }

    @Override
    public String toString() {
        return "ServiceObject<" + (this.id != null ? this.id : this.name) + ">";
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
     * Merge an object representation to the current instance
     *
     * @param json the JsonNode object representation
     */
    public void parse(JsonNode json) {
        parse(json.toString());
    }

    /**
     * Merge an object to the current instance
     *
     * @param serviceObject the object to merge from
     */
    public void parse(ServiceObject serviceObject) {

        id = serviceObject.id;
        userId = serviceObject.userId;
        parentId = serviceObject.parentId;
        path = serviceObject.path;

        name = serviceObject.name;
        description = serviceObject.description;

        createdAt = serviceObject.createdAt;
        updatedAt = serviceObject.updatedAt;

        customFields.clear();
        customFields.putAll(serviceObject.customFields);

        getStreams().clear();
        serviceObject.getStreams().entrySet().stream().forEach((el) -> {
            el.getValue().setServiceObject(this);
            getStreams().put(el.getKey(), el.getValue());
        });

        getActions().clear();
        serviceObject.getActions().entrySet().stream().forEach((el) -> {
            el.getValue().setServiceObject(this);
            getActions().put(el.getKey(), el.getValue());
        });

        isNew = (id == null);
    }

    @Override
    public void parse(String json) {
        try {
            parse(mapper.readValue(json, ServiceObject.class));
        } catch (IOException ex) {
            throw new ParserException(ex);
        }
    }

    /**
     * Map a JSON string properties to a ServiceObject instance
     *
     * @param json the json data
     * @return the ServiceObject instance
     */
    public static ServiceObject fromJSON(String json) {
        try {
            return mapper.readValue(json, ServiceObject.class);
        } catch (IOException e) {
            throw new RaptorComponent.ParserException(e);
        }
    }

    /**
     * Map JsonNode properties to a ServiceObject instance
     *
     * @param json the JsonNode data
     * @return the ServiceObject instance
     */
    public static ServiceObject fromJSON(JsonNode json) {
        return mapper.convertValue(json, ServiceObject.class);
    }

    @JsonIgnore
    public boolean isNew() {
        return isNew;
    }

    /**
     * Return the JsonNode representing the object
     *
     * @return the JsonNode representing the object
     */
    public ObjectNode toJsonNode() {
        ObjectNode node = getMapper().convertValue(this, ObjectNode.class);
        return node;
    }

    /**
     * Return the JSON string representing the object
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
     * Add a list of streams to the object
     *
     * @param streams list of streams
     */
    public void addStreams(Collection<Stream> streams) {
        streams.stream().forEach((stream) -> {

            stream.setServiceObject(this);

            stream.channels.values().stream().forEach((channel) -> {
                channel.setServiceObject(this);
            });

            this.streams.put(stream.name, stream);
        });
    }

    /**
     * Add a list of actions to the object
     *
     * @param values list of actions
     */
    public void addActions(Collection<Action> values) {
        values.stream().forEach((action) -> {
            action.setServiceObject(this);
            this.actions.put(action.name, action);
        });
    }

    /**
     * Return the current ServiceObject instance
     *
     * @return self-instance
     */
    @Override
    public ServiceObject getServiceObject() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServiceObject) {
            ServiceObject sobj = (ServiceObject) obj;
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
