/*
 * Copyright 2017 FBK/CREATE-NET
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.createnet.raptor.models.data.types.NumberRecord;
import org.createnet.raptor.models.data.types.BooleanRecord;
import org.createnet.raptor.models.data.types.GeoPointRecord;
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
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.models.objects.serializer.RecordSetSerializer;
import org.createnet.raptor.models.objects.deserializer.RecordSetDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@JsonSerialize(using = RecordSetSerializer.class)
@JsonDeserialize(using = RecordSetDeserializer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document
public class RecordSet {
    
    @Id
    @Indexed
    public Date timestamp;
    
    @Indexed
    final public Map<String, IRecord> channels = new HashMap();

    public String userId;
    
    @Indexed
    public String streamId;
    
    @Indexed
    public String objectId;

    @JsonIgnore
    @Transient
    private final Logger logger = LoggerFactory.getLogger(RecordSet.class);

    @JsonIgnore
    @Transient
    private Stream stream;

    public RecordSet() {
        this.timestamp = new Date();
    }

    public RecordSet(Stream stream) {
        this();
        this.setStream(stream);
    }

    public RecordSet(Stream stream, JsonNode row) {
        this(stream);
        parseJson(stream, row);
    }

    public RecordSet(Stream stream, String body) {
        this(stream);
        ObjectMapper mapper = Device.getMapper();
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

        this.timestamp = date;
    }

    public RecordSet(ArrayList<IRecord> records, Date date, String userId) {
        this(records, date);
        this.userId = userId;
    }

    public IRecord createRecord(String key, Object value) {
        if (this.getStream() == null) {
            throw new RecordsetException("Stream must be set");
        }
        return RecordSet.createRecord(this.getStream(), key, value);
    }
    
    public static IRecord createRecord(Stream stream, String key, Object value) {

        IRecord record = null;
        Channel channel = null;

        if (stream != null) {

            if (!stream.channels.isEmpty() && !stream.channels.containsKey(key)) {
                return null;
            }

            channel = stream.channels.get(key);
        } else {

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

    public ObjectNode toJsonNode() {
        try {
            return Device.getMapper().convertValue(this, ObjectNode.class);
        } catch (IllegalArgumentException ex) {
            throw new RaptorComponent.ParserException(ex);
        }
    }

    public String toJson() {
        return toJsonNode().toString();
    }

    @Override
    public String toString() {
        try {
            return toJson();
        } catch (RaptorComponent.ParserException ex) {
            logger.error("Cannot serialize RecordSet: {}", ex.getMessage());
        }
        return "{}";
    }

    /**
     * Get the records timestamp
     * @return 
     */
    public Date getTimestamp() {
        if (timestamp == null) {
            setTimestamp(new Date());
        }
        return timestamp;
    }

    /**
     * Get the records timestamp as UNIX epoch
     * @return
     */
    public Long getTimestampTime() {
        return getTimestamp().toInstant().getEpochSecond();
    }

    /**
     * Set the records timestamp
     * @param timestamp
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get the stored records
     * @return
     */
    public Map<String, IRecord> getRecords() {
        return channels;
    }

    /**
     * Set all the record values, removing the previous one if any
     * @param records
     */
    public void setRecords(List<IRecord> records) {
        this.channels.clear();

        for (IRecord record : records) {
            this.channels.put(record.getName(), record);
        }
    }
    
    /**
     * Add a record value
     * 
     * @param record
     */
    public void addRecord(IRecord record) {
        this.channels.put(record.getName(), record);
    }
    
    /**
     * Add a record value
     * 
     * @param channelName
     * @param value
     * @return 
     */
    public IRecord<?> addRecord(String channelName, Object value) {
        IRecord r = createRecord(channelName, value);
        if (r == null) {
            throw new RecordsetException("Can not create a Record, is the channel name listed in the stream channels?");
        }
        this.channels.put(r.getName(), r);
        return r;
    }

    private void parseJson(Stream stream, JsonNode row) {

        JsonNode channels = row;
        if (row.has("channels")) {

            if (row.has("timestamp")) {
                Date date = Date.from(Instant.ofEpochSecond(row.get("timestamp").asLong()));
                this.setTimestamp(date);
            } else if (row.has("lastUpdate")) {
                Date date = Date.from(Instant.ofEpochSecond(row.get("lastUpdate").asLong()));
                this.setTimestamp(date);
            }

            channels = row.get("channels");
        }

        for (Iterator<Map.Entry<String, JsonNode>> iterator = channels.fields(); iterator.hasNext();) {

            Map.Entry<String, JsonNode> item = iterator.next();

            String channelName = item.getKey();
            JsonNode nodeValue = item.getValue();

            JsonNode valObj = nodeValue;

            // allow short-hand without [current-]value
            if (nodeValue.isObject()) {
                if (nodeValue.has("value")) {
                    valObj = nodeValue.get("value");
                } else if (nodeValue.has("current-value")) {
                    valObj = nodeValue.get("current-value");
                }
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

    protected IRecord addRecord(Stream stream, String channelName, Object value) {

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

    public static RecordSet fromJSON(String raw) {
        try {
            return Device.getMapper().readValue(raw, RecordSet.class);
        } catch (IOException ex) {
            throw new RaptorComponent.ParserException(ex);
        }
    }

    public static RecordSet fromJSON(JsonNode raw) {
        return Device.getMapper().convertValue(raw, RecordSet.class);
    }

    public void setStream(Stream stream) {

        this.stream = stream;

        if (stream != null) {

            this.streamId = stream.name;

            if (stream.getDevice() != null) {

                this.objectId = stream.getDevice().getId();

                if (this.userId == null) {
                    this.userId = stream.getDevice().getUserId();
                }

            }

        }

    }

    public Stream getStream() {
        return this.stream;
    }

    public void validate() {

        if (getStream() != null) {

//      for (String channelName : stream.channels.keySet()) {
//        if (!channels.containsKey(channelName)) {
//          throw new RaptorComponent.ValidationException("Missing channel: " + channelName);
//        }
//      }
            for (String channelName : channels.keySet()) {
                if (!getStream().channels.containsKey(channelName)) {
                    throw new RaptorComponent.ValidationException("Objet model does not define this channel: " + channelName);
                }
            }

        }

    }

}
