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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luca Capra <luca.capra@gmail.com>
 */
//@JsonSerialize(using = ServiceObjectSerializer.class)
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

    public Long createdAt = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    public Long updatedAt = createdAt;

    final public Map<String, Object> customFields = new HashMap();
    final public Settings settings = new Settings();

    final public Map<String, Stream> streams = new HashMap();
    final public Map<String, Subscription> subscriptions = new HashMap();
    final public Map<String, Action> actions = new HashMap();

    public void addStreams(Collection<Stream> streams) {
        streams.stream().forEach((stream) -> {

            stream.setServiceObject(this);

            stream.channels.values().stream().forEach((channel) -> {
                channel.setServiceObject(this);
            });

            this.streams.put(stream.name, stream);
        });
    }

    public void addActions(Collection<Action> values) {
        values.stream().forEach((action) -> {
            action.setServiceObject(this);
            this.actions.put(action.name, action);
        });
    }

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

    public boolean isRoot() {
        return parentId == null;
    }

    public void setUpdateTime() {
        updatedAt = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void validate() {

        if (this.name == null) {
            throw new ValidationException("name field missing");
        }

        if (!this.isNew() && this.id == null) {
            throw new ValidationException("id field missing");
        }

        if (!this.streams.isEmpty()) {
            for (Map.Entry<String, Stream> item : this.streams.entrySet()) {
                item.getValue().validate();
            }
        }

        if (!this.actions.isEmpty()) {
            for (Map.Entry<String, Action> item : this.actions.entrySet()) {
                item.getValue().validate();
            }
        }

        if (!this.subscriptions.isEmpty()) {
            for (Map.Entry<String, Subscription> item : this.subscriptions.entrySet()) {
                item.getValue().validate();
            }
        }

    }

    public void parse(JsonNode json) {
        parse(json.toString());
    }

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

        streams.clear();
        serviceObject.streams.entrySet().stream().forEach((el) -> {
            el.getValue().setServiceObject(this);
            streams.put(el.getKey(), el.getValue());
        });

        subscriptions.clear();
        serviceObject.subscriptions.entrySet().stream().forEach((el) -> {
            el.getValue().setServiceObject(this);
            subscriptions.put(el.getKey(), el.getValue());
        });

        actions.clear();
        serviceObject.actions.entrySet().stream().forEach((el) -> {
            el.getValue().setServiceObject(this);
            actions.put(el.getKey(), el.getValue());
        });

        isNew = (id == null);
    }

    @Override
    public void parse(String json) {

        ServiceObject serviceObject;
        try {
            parse(mapper.readValue(json, ServiceObject.class));
        } catch (IOException ex) {
            throw new ParserException(ex);
        }
    }

    public static ServiceObject fromJSON(String json) {
        try {
            return mapper.readValue(json, ServiceObject.class);
        } catch (IOException e) {
            throw new RaptorComponent.ParserException(e);
        }
    }

    public static ServiceObject fromJSON(JsonNode json) {
        return mapper.convertValue(json, ServiceObject.class);
    }

    @JsonIgnore
    public boolean isNew() {
        return isNew;
    }

    public ObjectNode toJsonNode() {
        ObjectNode node = getMapper().convertValue(this, ObjectNode.class);
        return node;
    }

    public String toJSON() {
        String json = null;
        try {
            json = getMapper().writeValueAsString(this);
            return json;

        } catch (JsonProcessingException ex) {
            throw new ParserException(ex);
        }

    }

    @Override
    public ServiceObject getServiceObject() {
        return this;
    }

}
