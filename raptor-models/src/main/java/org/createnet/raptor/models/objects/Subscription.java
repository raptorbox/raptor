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

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class Subscription extends StreamContainer {

    org.slf4j.Logger logger = LoggerFactory.getLogger(Subscription.class);    

    public String id;
    public String type;
    public String callback;
    public String destination;
    public String source;
    public String updatedAt;
    public String createdAt;
    public String expire;

    public Subscription(String json, Stream stream) {
        initialize();
        JsonNode tree;
        try {
            tree = mapper.readTree(json);
        } catch (IOException ex) {
            throw new ParserException(ex);
        }
        parse(tree, stream);
    }

    public Subscription(JsonNode json, Stream stream) {
        initialize();
        parse(json, stream);
    }
    
    public Subscription(String json) {
        initialize();
        JsonNode tree;
        try {
            tree = mapper.readTree(json);
        } catch (IOException ex) {
            throw new ParserException(ex);
        }
        parse(tree, null);
    }

    public Subscription(JsonNode json) {
        initialize();
        parse(json, null);
    }
    
    public Subscription() {
        initialize();
    }
    
    protected void initialize() {}
    
    public void parse(String raw) throws ParserException {
        try {
            parse(mapper.readTree(raw));
        } catch (IOException ex) {
            throw new ParserException(ex);
        }
    }
    
    protected void parse(JsonNode json, Stream stream) {
        if(stream != null) 
            this.setStream(stream);
        parse(json);
    }
    
    protected void parse(JsonNode json) {
        
        id = json.get("id").asText();
        type = json.get("type").asText();
        callback = json.get("callback").asText();
        destination = json.get("destination").asText();
        source = json.get("source").asText();
        createdAt = json.get("createdAt").asText();
        updatedAt = json.get("updatedAt").asText();
        expire = json.get("expire").asText();
        
    }
    
    @Override
    public void validate() {
        throw new ValidationException("Not implemented");
    }
    
}
