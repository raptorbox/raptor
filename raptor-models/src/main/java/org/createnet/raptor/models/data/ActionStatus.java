/*
 * Copyright 2016 Luca Capra <lcapra@create-net.org>.
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
package org.createnet.raptor.models.data;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@JsonFilter("statusFilter")
public class ActionStatus {

  public enum ViewType {
    Internal, Public
  }
  
  public String id;
  public String status;
  public int createdAt;

  public String actionId;
  public String objectId;

  public ActionStatus() {
  }

  public ActionStatus(Action action, String status) {
    this(status);
    this.actionId = action.name;
    this.objectId = action.getServiceObject().id;
  }

  public ActionStatus(String status) {
    this.status = status;
    this.createdAt = (int) (System.currentTimeMillis() / 1000);
    this.id = UUID.randomUUID().toString();
  }

  public String toJSON() throws RaptorComponent.ParserException {
    return this.toJSON(ViewType.Public);
  }

  public String toJSON(ViewType type) throws RaptorComponent.ParserException {

    ObjectMapper mapper = ServiceObject.getMapper();

    try {

      Set<String> filterFields = new HashSet<>();
      switch (type) {
        case Internal:
          break;
        case Public:
        default:
          filterFields.add("actionId");
          filterFields.add("objectId");
          break;
      }

      FilterProvider filter = new SimpleFilterProvider()
        .addFilter("statusFilter",
                SimpleBeanPropertyFilter.serializeAllExcept(filterFields));

      String json = mapper.writer(filter).writeValueAsString(this);
      return json;
      
    } catch (JsonProcessingException ex) {
      throw new RaptorComponent.ParserException(ex);
    }

  }

  public static ActionStatus parseJSON(String rawStatus) throws IOException {
    return ServiceObject.getMapper().readValue(rawStatus, ActionStatus.class);
  }

  @Override
  public String toString() {
    try {
      return toJSON();
    } catch (RaptorComponent.ParserException ex) {
      return null;
    }
  }

}
