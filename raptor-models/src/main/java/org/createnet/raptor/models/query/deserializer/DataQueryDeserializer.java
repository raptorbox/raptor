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
package org.createnet.raptor.models.query.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import org.createnet.raptor.models.query.DataQuery;
import org.createnet.raptor.models.query.IQuery;
import org.createnet.raptor.models.query.NumberQuery;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class DataQueryDeserializer extends AbstractQueryDeserializer<DataQuery> {


    @Override
    public DataQuery deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        DataQuery query = new DataQuery();
        JsonNode node = jp.getCodec().readTree(jp);

        if (!node.has("channels")) {
            return null;
        }
        
        JsonNode nodeInner = node.get("channels");
        Map<String, IQuery> channelName = query.getChannels();
        
        if(channelName.containsKey(nodeInner.textValue()) && nodeInner.has("between")) {
        	JsonNode between = nodeInner.get("between");
        	if (between.isArray()) {
        		NumberQuery numQ = (NumberQuery) channelName.get(nodeInner.textValue());
                for (int i = 0; i < between.size(); i++) {
                	numQ.between(between.get(0).asInt(), between.get(1).asInt());
                }
                query.getChannels().put(nodeInner.textValue(), numQ);
            }
        }
        
        return query;
    }

}
