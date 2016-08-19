/*
 * Copyright 2016 CREATE-NET
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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.createnet.raptor.models.data.types.NumberRecord;
import org.createnet.raptor.models.data.types.BooleanRecord;
import org.createnet.raptor.models.data.types.GeoPointRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.createnet.raptor.models.data.types.StringRecord;
import org.createnet.raptor.models.data.types.TypesManager;
import org.createnet.raptor.models.exception.RecordsetException;
import org.createnet.raptor.models.objects.Channel;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.models.objects.serializer.RecordSetSerializer;
import org.createnet.raptor.models.objects.deserializer.RecordSetDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@JsonSerialize(using = RecordSetSerializer.class)
@JsonDeserialize(using = RecordSetDeserializer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("publicFieldsFilter")
public class RecordSet {

  public Date lastUpdate;
  final public Map<String, IRecord> channels = new HashMap();

  public String userId;
  public String streamId;
  public String objectId;
  
  
  @JsonIgnore
  private final Logger logger = LoggerFactory.getLogger(RecordSet.class);
  
  @JsonIgnore
  private Stream stream;

  
  public RecordSet() {
    this.lastUpdate = new Date();
  }
  
  public RecordSet(Stream stream) {
    this();
    this.stream = stream;
  }

  public RecordSet(Stream stream, JsonNode row) throws RecordsetException {
    this(stream);
    parseJson(stream, row);
  }

  public RecordSet(Stream stream, String body) throws RecordsetException {
    this(stream);
    ObjectMapper mapper = ServiceObject.getMapper();
    try {
      parseJson(stream, mapper.readTree(body));
    } catch (IOException ex) {
      throw new RecordsetException(ex);
    }
  }

  public RecordSet(ArrayList<IRecord> records) {
    this();
    for (IRecord record : records) {
      this.channels.put(record.getName(), record);
    }
  }

  public RecordSet(ArrayList<IRecord> records, Date date) {

    for (IRecord record : records) {
      this.channels.put(record.getName(), record);
    }
    
    this.lastUpdate = date;
  }

  public static IRecord createRecord(Stream stream, String key, Object value) throws RaptorComponent.ParserException {

    IRecord record = null;
    Channel channel = null;
    
    if (stream != null) {

      if (!stream.channels.isEmpty() && !stream.channels.containsKey(key)) {
        return null;
      }

      channel = stream.channels.get(key);
    } 
    else {

      //try parse value
      for (Map.Entry<String, Record> item : TypesManager.getTypes().entrySet()) {

        try {

          Record recordType = item.getValue();
          value = recordType.parseValue(value);

          channel = new Channel();
          channel.name = key;
          channel.type = recordType.getType();

          break;

        } catch (RaptorComponent.ParserException e) {
//          int v = 0;
        }

      }

    }

    if (channel != null) {

      switch (channel.type.toLowerCase()) {
        case "string":
          record = new StringRecord();
          break;
        case "boolean":
          record = new BooleanRecord();
          break;
        case "number":
          record = new NumberRecord();
          break;
        case "geo_point":
          record = new GeoPointRecord();
          break;
        default:
          throw new RaptorComponent.ParserException("Data type not supported: " + channel.type);
      }

      record.setValue(value);
      record.setChannel(channel);
    }

    return record;
  }

  public ObjectNode toJsonNode() throws JsonProcessingException, IOException {
    return ServiceObject.getMapper().convertValue(this, ObjectNode.class);
  }
  
  public String toJson() throws JsonProcessingException, IOException {
    return toJsonNode().toString();
  }

  @Override
  public String toString() {
    try {
      return toJson();
    } catch (IOException ex) {
      logger.error("Cannot serialize RecordSet: {}", ex.getMessage());
    }
    return "{}";
  }
  
  
  public Date getLastUpdate() {
    if (lastUpdate == null) {
      setLastUpdate(new Date());
    }
    return lastUpdate;
  }

  public Long getLastUpdateTime() {
    return getLastUpdate().toInstant().getEpochSecond();
  }

  public void setLastUpdate(Date lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

  public Map getRecords() {
    return channels;
  }

  public void setRecords(List<IRecord> records) {
    this.channels.clear();

    for (IRecord record : records) {
      this.channels.put(record.getName(), record);
    }
  }

  private void parseJson(Stream stream, JsonNode row) throws RecordsetException {


    JsonNode channels = row;
    if (row.has("channels")) {
      
      if (row.has("lastUpdate")) {
        Date date = Date.from(Instant.ofEpochSecond(row.get("lastUpdate").asLong()));
        this.setLastUpdate(date);
      }
      
      channels = row.get("channels");
    }

    for (Iterator<Map.Entry<String, JsonNode>> iterator = channels.fields(); iterator.hasNext();) {

      Map.Entry<String, JsonNode> item = iterator.next();

      String channelName = item.getKey();
      JsonNode nodeValue = item.getValue();

      // allow short-hand without current-value
      JsonNode valObj = nodeValue;
      if (nodeValue.has("current-value")) {
        valObj = nodeValue.get("current-value");
      }

      try {
        if (stream != null && (stream.channels != null && !stream.channels.isEmpty())) {
          if (stream.channels.containsKey(channelName)) {
            this.addRecord(stream, channelName, valObj);
          }
        } else {
          // definition is unknown, add all channels to the record set
          this.addRecord(stream, channelName, valObj);
        }
      } catch (Exception e) {
        throw new RecordsetException(e);
      }

    }

  }

  protected IRecord addRecord(Stream stream, String channelName, Object value) throws RaptorComponent.ParserException {

    IRecord record = RecordSet.createRecord(stream, channelName, value);
    if (record != null) {
      record.setRecordSet(this);
      this.channels.put(record.getName(), record);
    }

    return record;
  }

  /**
   * @param channelName
   * @return IRecord
   */
  public IRecord getByChannelName(String channelName) {
    return channels.get(channelName);
  }

  /**
   * @param channel
   * @return IRecord
   */
  public IRecord getByChannel(Channel channel) {
    return getByChannelName(channel.name);
  }

  public static RecordSet fromJSON(String raw) throws IOException {
    return ServiceObject.getMapper().readValue(raw, RecordSet.class);
  }

  public void setStream(Stream stream) {
    this.stream = stream;
  }
  
  public Stream getStream() {
    return this.stream;
  }
  
}
