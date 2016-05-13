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
package org.createnet.raptor.models.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.createnet.raptor.models.exception.RecordsetException;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class ResultSet {
  
  // json properties
  public final Data data = new Data();
  
  @JsonIgnore
  public int lastUpdate;
  
  @JsonIgnore
  protected Stream stream;
    
  @JsonIgnore
  private final Logger logger = LoggerFactory.getLogger(ResultSet.class);
  
  static public class Data extends ArrayList<RecordSet>{
    
    public Data() {}
    
  }
  
  public Stream getStream() {
    return stream;
  }

  public void setStream(Stream stream) {
    this.stream = stream;
  }

  public ResultSet() {
  }
  
  public ResultSet(Stream stream, String jsonString) throws RecordsetException {
    this.stream = stream;
    if (jsonString != null) {
      parse(jsonString);
    }
  }

  public ResultSet(Stream stream) throws RecordsetException {
    this(stream, null);
  }

  private void parse(String jsonString) throws RecordsetException {

    ObjectMapper mapper = ServiceObject.getMapper();
    JsonNode json;

    try {
      json = mapper.readTree(jsonString);
    } catch (IOException ex) {
      logger.error("Error parsing: " + jsonString, ex);
      return;
    }

    JsonNode list = json.get("data");

    if (list.isArray()) {
      for (JsonNode row : list) {
        this.add(jsonString);
      }
    }

  }
  
  public Iterator<RecordSet> iterator() {
    return data.iterator();
  }
  
  public int size() {
    return data.size();
  }
  
  public RecordSet add(RecordSet recordset) {
    this.data.add(recordset);
    return recordset;
  }
  
  public RecordSet add(String raw) throws RecordsetException {
    RecordSet recordset = new RecordSet(stream, raw);
    return this.add(recordset);
  }
  
  public String toJson() throws JsonProcessingException, IOException {
    return toJsonNode().toString();
  }
  
  public ObjectNode toJsonNode() throws JsonProcessingException, IOException {
    ObjectMapper mapper = ServiceObject.getMapper();
    return mapper.convertValue(this, ObjectNode.class);
  }

}
