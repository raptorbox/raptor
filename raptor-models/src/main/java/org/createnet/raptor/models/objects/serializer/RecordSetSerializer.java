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
package org.createnet.raptor.models.objects.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Map;
import org.createnet.raptor.models.data.IRecord;
import org.createnet.raptor.models.data.RecordSet;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class RecordSetSerializer extends JsonSerializer<RecordSet> {

    @Override
    public void serialize(RecordSet r, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonProcessingException {

        jg.writeStartObject();
        
        jg.writeObjectFieldStart("channels");
        
        for(Map.Entry<String, IRecord> item : r.channels.entrySet()) {
          
          String channelName = item.getKey();
          IRecord channel = item.getValue();
          
          if(channel == null) continue;
          if(channel.getValue() == null) continue;
          
          jg.writeObjectFieldStart(channelName);
          jg.writeObjectField("current-value", channel.getValue());
          jg.writeEndObject();
          
        }
        
        jg.writeEndObject();
        
        jg.writeNumberField("lastUpdate", r.getLastUpdateTime());
        
        if(r.userId != null)
          jg.writeStringField("userId", r.userId);
        
        if(r.streamId != null)
          jg.writeStringField("streamId", r.streamId);
        
        
        jg.writeEndObject();

    }
    
}
