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
package org.createnet.raptor.models.objects.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Iterator;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.objects.Channel;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class RecordSetDeserializer extends JsonDeserializer<RecordSet> {

    final private long past = 90000000000L; //Tue Nov 07 1972 17:00:00 GMT+0100 (CET)

    @Override
    public RecordSet deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

        JsonNode tree = jp.getCodec().readTree(jp);
        RecordSet recordset = new RecordSet();

        if (tree.has("userId")) {
            recordset.userId(tree.get("userId").asText());
        }

        if (tree.has("objectId")) {
            recordset.deviceId(tree.get("objectId").asText());
        }
        if (tree.has("deviceId")) {
            recordset.deviceId(tree.get("deviceId").asText());
        }

        if (tree.has("streamId")) {
            recordset.streamId(tree.get("streamId").asText());
        }

        if (tree.has("location")
                && (tree.get("location").has("x") && tree.get("location").has("y"))) {
            Double x = tree.get("location").get("x").asDouble();
            Double y = tree.get("location").get("y").asDouble();
            recordset.location(new GeoJsonPoint(x, y));
        }

        long time = System.currentTimeMillis();
        if (tree.has("channels")) {
            
            if (tree.has("timestamp")) {
                long time1 = tree.get("timestamp").asLong();
                if (time1 > 0 && time1 < past) {
                    time = time1 * 1000;
                }
            } else if (tree.has("lastUpdate")) {
                long time1 = tree.get("lastUpdate").asLong();
                if (time1 > 0 && time1 < past) {
                    time = time1 * 1000;
                }
            }
            
            tree = tree.get("channels");
        }

        recordset.timestamp(time);

        if (tree.isObject()) {

            Iterator<String> it = tree.fieldNames();
            while (it.hasNext()) {

                String channelName = it.next();
                JsonNode channelNode = tree.get(channelName);

                if (channelNode.isObject()) {
                    if (channelNode.has("current-value")) {
                        channelNode = channelNode.get("current-value");
                    }
                }

                Channel channel = new Channel();
                channel.name(channelName);

                Object channelValue = RecordSet.parseType(channelNode, channel);
                if (channelValue != null) {
                    recordset.channel(channelName, channelValue);
                }

            }
        }

        return recordset;
    }
}
