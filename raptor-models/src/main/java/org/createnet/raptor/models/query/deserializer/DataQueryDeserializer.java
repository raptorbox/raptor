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
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.createnet.raptor.models.query.BoolQuery;
import org.createnet.raptor.models.query.DataQuery;
import org.createnet.raptor.models.query.IQuery;
import org.createnet.raptor.models.query.NumberQuery;
import org.createnet.raptor.models.query.TextQuery;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

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

        if (node.has("userId")) {
            query.userId(node.get("userId").asText());
        }

        if (node.has("channels")) {
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
                            // for (int i = 0; i < between.size(); i++) {
                            // numQ.between(between.get(0).asInt(), between.get(1).asInt());
                            // }
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
        }
        if (node.has("timestamp")) {
            JsonNode timestampNode = node.get("timestamp");

            if (timestampNode.has("between")) {
                JsonNode between = timestampNode.get("between");
                if (between.isArray()) {
                    if (!between.get(0).isNull() && !between.get(1).isNull()) {
                        query.timeRange(Instant.ofEpochMilli(between.get(0).asLong()),
                                Instant.ofEpochMilli(between.get(1).asLong()));
                    }
                }
            }
        }
        if (node.has("location")) {
            JsonNode locationNode = node.get("location");
            if (!locationNode.isNull() && locationNode.has("distance")) {
                JsonNode distanceNode = locationNode.get("distance");
                if (!distanceNode.isNull()) {
                    Iterator<Map.Entry<String, JsonNode>> fields = distanceNode.fields();
                    GeoJsonPoint geo = null;
                    double radius = 0.0;
                    Metrics unit = null;
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> entry = fields.next();
                        System.out.println(entry.getKey() + ":" + entry.getValue());

                        JsonNode jsonNode = distanceNode.get(entry.getKey());
                        if (!jsonNode.isNull()) {
                            // channelName.containsKey(nodeInner.textValue()) &&
                            if (jsonNode.has("center")) {
                                geo = getGeoJsonPoint(jsonNode, "center");
                                // JsonNode centerNode = jsonNode.get("center");
                                // geo = new GeoJsonPoint(centerNode.get("x").asDouble(),
                                // centerNode.get("y").asDouble());

                                // if (centerNode.isArray()) {
                                // NumberQuery numQ = new NumberQuery();
                                // GeoJsonPoint geo = new GeoJsonPoint(centerNode.get(0).asInt(),
                                // centerNode.get(1).asInt());
                                // }
                            }
                            if (jsonNode.has("radius")) {
                                radius = jsonNode.get("radius").asDouble();
                            }
                            if (jsonNode.has("unit")) {
                                unit = Metrics.valueOf(jsonNode.get("unit").asText());
                            }
                            query.distance(geo, radius, unit);
                        }

                    }
                }
            } else if (locationNode.has("boundingBox")) {
                JsonNode jsonNode = locationNode.get("boundingBox");
                if (!jsonNode.isNull()) {
                    GeoJsonPoint northWest = getGeoJsonPoint(jsonNode, "northWest");
                    GeoJsonPoint southWest = getGeoJsonPoint(jsonNode, "southWest");
                    query.boundingBox(northWest, southWest);
                }
            }
        }

        return query;
    }

    private GeoJsonPoint getGeoJsonPoint(JsonNode parent, String node) {
        JsonNode centerNode = parent.get(node);
        GeoJsonPoint geo = new GeoJsonPoint(centerNode.get("x").asDouble(), centerNode.get("y").asDouble());

        return geo;
    }
}
