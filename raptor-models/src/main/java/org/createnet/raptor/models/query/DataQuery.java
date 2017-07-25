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
package org.createnet.raptor.models.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.createnet.raptor.models.query.deserializer.DataQueryDeserializer;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonDeserialize(using = DataQueryDeserializer.class)
public class DataQuery extends BaseQuery {

    protected final NumberQuery timestamp = new NumberQuery();
    protected final Map<String, IQuery> channels = new HashMap();
    protected final GeoQuery location = new GeoQuery();
    protected String streamId = null;

    public DataQuery timeRange(Instant from, Instant to) {
        timestamp.between(from.toEpochMilli(), to.toEpochMilli());
        return this;
    }

    public DataQuery timeRange(Instant from) {
        return timeRange(from, Instant.now());
    }

    public DataQuery range(String channelName, Number from, Number to) {
        this.channels.put(channelName, new NumberQuery().between(from, to));
        return this;
    }

    public DataQuery match(String channelName, String match) {
        this.channels.put(channelName, new TextQuery().match(match));
        return this;
    }

    public DataQuery match(String channelName, boolean match) {
        this.channels.put(channelName, new BoolQuery(match));
        return this;
    }

    public DataQuery distance(GeoJsonPoint center, double radius, Metrics unit) {
        this.location.distance(center, radius, unit);
        return this;
    }

    public DataQuery distance(GeoJsonPoint center, double radius) {
        return distance(center, radius, Metrics.KILOMETERS);
    }

    public DataQuery boundingBox(GeoJsonPoint nw, GeoJsonPoint sw) {
        this.location.boundingBox(nw, sw);
        return this;
    }

    public DataQuery streamId(String s) {
        this.streamId = s;
        return this;
    }

    public NumberQuery getTimestamp() {
        return timestamp;
    }

    public Map<String, IQuery> getChannels() {
        return channels;
    }

    public GeoQuery getLocation() {
        return location;
    }

    public String getStreamId() {
        return streamId;
    }

}
