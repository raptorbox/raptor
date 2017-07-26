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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.createnet.raptor.models.query.BoolQuery;
import org.createnet.raptor.models.query.DataQuery;
import org.createnet.raptor.models.query.IQuery;
import org.createnet.raptor.models.query.NumberQuery;
import org.createnet.raptor.models.query.TextQuery;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class DataQueryDeserializer extends AbstractQueryDeserializer<DataQuery> {

	@Override
	public DataQuery deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {

		DataQuery query = new DataQuery();
		JsonNode node = jp.getCodec().readTree(jp);

		if (!node.has("channels")) {
			return null;
		}

		JsonNode channelNode = node.get("channels");
		Map<String, IQuery> channels = new HashMap<String, IQuery>();

		Iterator<Map.Entry<String, JsonNode>> fields = channelNode.fields();
		while (fields.hasNext()) {
			Map.Entry<String, JsonNode> entry = fields.next();
			System.out.println(entry.getKey() + ":" + entry.getValue());
			JsonNode jsonNode = channelNode.get(entry.getKey());
			if (!jsonNode.isNull()) {
				// channelName.containsKey(nodeInner.textValue()) &&
				if (jsonNode.has("between")) {
					JsonNode between = jsonNode.get("between");
					if (between.isArray()) {
						NumberQuery numQ = new NumberQuery();
//						for (int i = 0; i < between.size(); i++) {
//							numQ.between(between.get(0).asInt(), between.get(1).asInt());
//						}
						query.range(entry.getKey(), between.get(0).asInt(), between.get(1).asInt());
					}
				} else if (jsonNode.has("match")) {
					JsonNode match = jsonNode.get("match");
					BoolQuery boolQ = new BoolQuery(match.asBoolean());
					channels.put(jsonNode.textValue(), boolQ);
				} else if (jsonNode.has("contains")) {
					JsonNode contains = jsonNode.get("contains");
					TextQuery textQ = new TextQuery();
					textQ.contains(contains.asText());
					channels.put(jsonNode.textValue(), textQ);
				}
			}
		}

		return query;
	}

}
