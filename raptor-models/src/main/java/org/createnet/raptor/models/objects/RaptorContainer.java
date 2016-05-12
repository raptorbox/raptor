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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import java.util.HashMap;
import java.util.Map;
import org.createnet.raptor.models.events.IEventListener;
import org.createnet.raptor.models.objects.serializer.ServiceObjectView;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
abstract class RaptorContainer implements RaptorComponent {

  abstract public void validate() throws ValidationException;

  abstract public void parse(String json) throws ParserException;

  static protected final ObjectMapper mapper = new ObjectMapper();

  protected IEventListener listener;

  public static ObjectMapper getMapper() {
    return mapper;
  }
  
  @JsonBackReference
  protected Map<String, Object> extras = new HashMap<>();

  public Map<String, Object> getExtras() {
    return extras;
  }

  public void addExtra(String key, Object val) {
    extras.put(key, val);
  }

  @JsonBackReference
  protected RaptorComponent container;

  public RaptorComponent getContainer() {
    return container;
  }

  public void setContainer(RaptorComponent container) {
    this.container = container;
  }

  public ObjectNode toJsonNode() {
    ObjectNode node = mapper.convertValue(this, ObjectNode.class);

    if (!getExtras().isEmpty()) {
      getExtras().entrySet().stream().forEach((Map.Entry<String, Object> el) -> {

        String field = el.getKey();
        Object val = el.getValue();

        if (val instanceof String) {
          node.put(field, (String) val);
        }

        if (val instanceof Integer) {
          node.put(field, Integer.parseInt((String) val));
        }

        if (val instanceof Long) {
          node.put(field, Long.parseLong((String) val));
        }

      });
    }

    return node;
  }

  public String toJSON(ServiceObjectView type) throws ParserException {
    String json = null;
    try {

      FilterProvider filter;
      switch (type) {
        
        case Internal:
          json = mapper.writeValueAsString(this.toJsonNode());
          break;
          
        case IdOnly:

          // Hide internal fileds
          filter = new SimpleFilterProvider().addFilter("publicFieldsFilter",
                  SimpleBeanPropertyFilter.filterOutAllExcept("id"));
          
          json = mapper.writer(filter).writeValueAsString(this.toJsonNode());
          
          break;
        case Public:
        default:
          
          // Hide internal fileds
          filter = new SimpleFilterProvider().addFilter("publicFieldsFilter",
                  SimpleBeanPropertyFilter.serializeAllExcept("userId"));
          
          json = mapper.writer(filter).writeValueAsString(this.toJsonNode());
          
          break;
      }

    } catch (JsonProcessingException ex) {
      throw new ParserException(ex);
    }
    return json;
  }

  public String toJSON() throws ParserException {
    return toJSON(ServiceObjectView.Public);
  }

  public IEventListener getListener() {
    return listener;
  }

  public void setListener(IEventListener listener) {
    this.listener = listener;
  }

  protected boolean hasListener() {
    return getListener() != null;
  }

}
