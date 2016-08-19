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
package org.createnet.raptor.models.objects.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import org.createnet.raptor.models.data.Record;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.types.TypesManager;
import org.createnet.raptor.models.objects.Channel;
import org.createnet.raptor.models.objects.RaptorComponent;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class RecordSetDeserializer extends JsonDeserializer<RecordSet> {

  @Override
  public RecordSet deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

    JsonNode tree = jp.getCodec().readTree(jp);

    RecordSet recordset = new RecordSet();

    long time = System.currentTimeMillis();

    if (tree.has("channels")) {

      if (tree.has("lastUpdate")) {
        long time1 = tree.get("lastUpdate").asLong();
        if(time1 > 0) {
          time = time1 * 1000;
        }
      }
      tree = tree.get("channels");
    }

    recordset.setLastUpdate(new Date(time));
    
    if (tree.isObject()) {
      
      Iterator<String> it = tree.fieldNames();
      while (it.hasNext()) {
        
        String channelName = it.next();
        JsonNode channelNode = tree.get(channelName);

        if (channelNode.isObject() && channelNode.has("current-value")) {
          channelNode = channelNode.get("current-value");
        }

        for (Map.Entry<String, Record> item : TypesManager.getTypes().entrySet()) {
          try {

            Record recordType = item.getValue();
            Object val = recordType.parseValue(channelNode);

            Record instance = (Record) recordType.getClass().newInstance();
            instance.setValue(val);
            Channel channel = new Channel();
            
            channel.name = channelName;
            channel.type = instance.getType();
            instance.setChannel(channel);
            
            recordset.channels.put(channelName, instance);
            break;
            
          } catch (RaptorComponent.ParserException | InstantiationException | IllegalAccessException e) {
          }
        }

      }
    }

    return recordset;
  }
}
