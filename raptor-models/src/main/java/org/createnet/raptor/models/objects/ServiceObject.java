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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFilter;
import org.createnet.raptor.models.objects.serializer.ServiceObjectSerializer;
import org.createnet.raptor.models.objects.deserializer.ServiceObjectDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@JsonSerialize(using = ServiceObjectSerializer.class)
@JsonDeserialize(using = ServiceObjectDeserializer.class)
@JsonFilter("publicFieldsFilter")
public class ServiceObject extends ServiceObjectContainer {

  Logger logger = LoggerFactory.getLogger(ServiceObject.class);

  @JsonBackReference
  private boolean isNew = true;

  public String userId;
  
  public String id;

  public String name;
  public String description = "";

  public Long createdAt;
  public Long updatedAt;

  public Map<String, Object> customFields;
  public Map<String, String> properties;

  public Map<String, Stream> streams;
  public Map<String, Subscription> subscriptions;
  public Map<String, Action> actions;

  public static String generateUUID() {
    return UUID.randomUUID().toString();
  }

  public ServiceObject() {
    initialize();
  }

  public ServiceObject(String soid) {
    initialize();
    this.id = soid;
  }

  private void initialize() {

    customFields = new HashMap<>();
    properties = new HashMap<>();

    streams = new HashMap<>();
    subscriptions = new HashMap<>();
    actions = new HashMap<>();

    createdAt = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    updatedAt = createdAt;

    addExtra("public", true);
  }

  @Override
  public String toString() {
    return "ServiceObject<" + this.id + ">";
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
    customFields = serviceObject.customFields;
    properties = serviceObject.properties;
    createdAt = serviceObject.createdAt;
    updatedAt = serviceObject.updatedAt;
    streams = serviceObject.streams;
    subscriptions = serviceObject.subscriptions;
    actions = serviceObject.actions;

    isNew = (id == null);

    serviceObject = null;

  }

  public boolean isNew() {
    return isNew;
  }

}
