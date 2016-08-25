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

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.createnet.raptor.models.objects.deserializer.ServiceObjectDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import static org.createnet.raptor.models.objects.RaptorContainer.mapper;
import org.createnet.raptor.models.objects.serializer.ServiceObjectView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luca Capra <luca.capra@gmail.com>
 */
//@JsonSerialize(using = ServiceObjectSerializer.class)
@JsonDeserialize(using = ServiceObjectDeserializer.class)
@JsonFilter("objectFieldsFilter")
public class ServiceObject extends ServiceObjectContainer {

  Logger logger = LoggerFactory.getLogger(ServiceObject.class);

  @JsonIgnore
  private boolean isNew = true;

  public String userId;

  public String id;

  public String name;
  public String description = "";

  public Long createdAt = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
  public Long updatedAt = createdAt;

  public Map<String, Object> customFields = new HashMap();
  public Settings settings = new Settings();

  public Map<String, Stream> streams = new HashMap();
  public Map<String, Subscription> subscriptions = new HashMap();
  public Map<String, Action> actions = new HashMap();

  public void addStreams(Collection<Stream> streams) {
    for (Stream stream : streams) {
      stream.setServiceObject(this);
      for (Channel channel : stream.channels.values()) {
        channel.setServiceObject(this);
      }
      this.streams.put(stream.name, stream);
    }
  }

  public void addActions(Collection<Action> values) {
    for (Action action : values) {
      action.setServiceObject(this);
      this.actions.put(action.name, action);
    }
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
    return "ServiceObject<" + (this.id == null ? this.id : this.name) + ">";
  }

  public String getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUpdateTime() {
    updatedAt = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
  }

  @Override
  public void validate() throws ValidationException {

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

  public void parse(JsonNode json) throws ParserException {
    parse(json.toString());
  }  
  
  @Override
  public void parse(String json) throws ParserException {

    ServiceObject serviceObject;
    try {
      serviceObject = mapper.readValue(json, ServiceObject.class);
    } catch (IOException ex) {
      throw new ParserException(ex);
    }

    id = serviceObject.id;
    userId = serviceObject.userId;
    name = serviceObject.name;
    description = serviceObject.description;

    createdAt = serviceObject.createdAt;
    updatedAt = serviceObject.updatedAt;

    customFields.clear();
    customFields.putAll(serviceObject.customFields);

    streams.clear();
    for (Map.Entry<String, Stream> el : serviceObject.streams.entrySet()) {
      el.getValue().setServiceObject(this);
      streams.put(el.getKey(), el.getValue());
    }

    subscriptions.clear();
    for (Map.Entry<String, Subscription> el : serviceObject.subscriptions.entrySet()) {
      el.getValue().setServiceObject(this);
      subscriptions.put(el.getKey(), el.getValue());
    }

    actions.clear();
    for (Map.Entry<String, Action> el : serviceObject.actions.entrySet()) {
      el.getValue().setServiceObject(this);
      actions.put(el.getKey(), el.getValue());
    }

    isNew = (id == null);
  }

  public static ServiceObject fromJSON(String json) throws ParserException {
    try {
      return mapper.readValue(json, ServiceObject.class);
    } catch (IOException e) {
      throw new RaptorComponent.ParserException(e);
    }
  }
  
  public static ServiceObject fromJSON(JsonNode json) {
    return mapper.convertValue(json, ServiceObject.class);
  }

  public boolean isNew() {
    return isNew;
  }

  protected ObjectMapper getMapper(ServiceObjectView type) {

    SimpleBeanPropertyFilter propertyFilter;
    switch (type) {

      case Internal:
        // all fields          
        propertyFilter = SimpleBeanPropertyFilter.serializeAllExcept();
        break;
      case IdOnly:
        // Keep ids only
        propertyFilter = SimpleBeanPropertyFilter.filterOutAllExcept("id");
        break;
      case Public:
      default:
        // Hide internal fileds
        propertyFilter = SimpleBeanPropertyFilter.serializeAllExcept("userId");
        break;
    }
    
    FilterProvider filter = new SimpleFilterProvider()
            .addFilter("objectFieldsFilter", propertyFilter);
    
    ObjectMapper mapper1 = ServiceObject.getMapper();
    mapper1.setFilterProvider(filter);
    
    return mapper1;
    
  }

  public ObjectNode toJsonNode() {
    return toJsonNode(ServiceObjectView.Public);
  }
  
  public ObjectNode toJsonNode(ServiceObjectView type) {
    ObjectNode node = getMapper(type).convertValue(this, ObjectNode.class);
    return node;
  }

  public String toJSON(ServiceObjectView type) throws ParserException {
    String json = null;
    try {
      json = getMapper(type).writeValueAsString(this);
      return json;

    } catch (JsonProcessingException ex) {
      throw new ParserException(ex);
    }

  }
 
  public String toJSON() throws ParserException {
    return toJSON(ServiceObjectView.Public);
  }

  
}
