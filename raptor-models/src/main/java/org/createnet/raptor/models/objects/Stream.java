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
import com.fasterxml.jackson.core.JsonProcessingException;
import org.createnet.raptor.models.objects.serializer.StreamSerializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.createnet.raptor.models.data.IRecord;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.events.StreamEvent;
import org.createnet.raptor.models.exception.ClientException;
import org.createnet.raptor.models.exception.RecordsetException;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@JsonSerialize(using = StreamSerializer.class)
public class Stream extends StreamContainer {
    
    org.slf4j.Logger logger = LoggerFactory.getLogger(Stream.class);    
    
    public String name;
    public String type;
    public String description;
    
    final public Map<String, Channel> channels = new HashMap();
    
    public Stream(String json, ServiceObject object) throws IOException {
        initialize();
        JsonNode tree = mapper.readTree(json);
        parse(tree, object);
    }

    public Stream(JsonNode json, ServiceObject object) {
        initialize();
        parse(json, object);
    }
    
    public Stream(String name, JsonNode json, ServiceObject object) {
        initialize();
        this.name = name;
        parse(json, object);
    }
    
    public Stream(String json) throws IOException {
        initialize();
        JsonNode tree = mapper.readTree(json);
        parse(tree, null);
    }

    public Stream(JsonNode json) {
        initialize();
        parse(json, null);
    }
    
    public Stream() {
        initialize();
    }
    
    protected void initialize() {
    }
    
    protected void parse(JsonNode json, ServiceObject object) {
        this.setServiceObject(object);
        parse(json);
    }
    
    protected void parse(JsonNode json) {
        
        if(json.has("name")) {
          name = json.get("name").asText();
        }
        
        if(json.has("type")) {
          type = json.get("type").asText();
        }
        
        if(json.has("description")) {
          description = json.get("description").asText();
        }
        
        if(json.has("channels")) {
          
          if(json.get("channels").isObject()) {
            
            Iterator<String> fieldNames = json.get("channels").fieldNames();
            while(fieldNames.hasNext()) {

                String name = fieldNames.next();
                JsonNode jsonChannel = json.get("channels").get(name);
                
                Channel channel = new Channel(name, jsonChannel);
                channel.setStream(this);
                channels.put(channel.name, channel);
            }
          }
          
          if(json.get("channels").isArray()) {
            for (JsonNode jsonChannel : json.get("channels")) {
                Channel channel = new Channel(jsonChannel);
                channel.setStream(this);
                channels.put(channel.name, channel);
            }
          }
          
            
        }
        
    }
    
    @Override
    public void parse(String json) throws ParserException {
        try {
            parse(mapper.readTree(json));
        } catch (IOException ex) {
            throw new ParserException(ex);
        }
    }
    
    @Override
    public void validate() throws ValidationException {
        throw new ValidationException("Not implemented yet."); 
    }
    
}
