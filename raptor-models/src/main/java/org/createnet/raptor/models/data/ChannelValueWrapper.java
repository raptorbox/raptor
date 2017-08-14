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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import org.createnet.raptor.models.exception.ValueConversionException;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class ChannelValueWrapper {

    private final Object raw;

    public ChannelValueWrapper(Object raw) {
        this.raw = raw;
    }

    /**
     * Return a geo position value
     *
     * @return
     */
    public GeoJsonPoint getPosition() {
        if (raw instanceof GeoJsonPoint) {
            return (GeoJsonPoint) raw;
        }
        if (raw instanceof double[]) {
            double[] p = (double[]) raw;
            return new GeoJsonPoint(p[0], p[1]);
        }
        if (raw instanceof float[]) {
            float[] p = (float[]) raw;
            return new GeoJsonPoint(p[0], p[1]);
        }
        if (raw instanceof JsonNode) {
            JsonNode json = (JsonNode) raw;
            if(json.isArray()) {
                return new GeoJsonPoint(json.get(0).asDouble(), json.get(1).asDouble());
            }
            if(json.has("lat") && json.has("lon")) {
                return new GeoJsonPoint(json.get("lat").asDouble(), json.get("long").asDouble());
            }
            if(json.has("latitude") && json.has("longitude")) {
                return new GeoJsonPoint(json.get("latitude").asDouble(), json.get("longitude").asDouble());
            }
        }
        throw new ValueConversionException("Cannot cast value to GeoJsonPoint");
    }

    /**
     * Return a list value
     *
     * @return
     */
    public List<Object> getList() {
        
        if(raw instanceof List) {
            return (List) raw;
        }
        
        if(raw instanceof JsonNode) {
            JsonNode j = (JsonNode) raw;
            if (j.isArray()) {
                List<Object> l = new ArrayList();
                for (JsonNode jsonNode : j) {
                    l.add(j);
                }
                return l;
            }
        }
        
        throw new ValueConversionException("Cannot cast value to GeoJsonPoint");
    }

    /**
     * Return an object value
     *
     * @return
     */
    public Object getObject() {
        return raw;
    }

    /**
     * Return a numeric value
     *
     * @return
     */
    public NumericValue getNumber() {
        return new NumericValue(raw);
    }

    /**
     * Return a string value
     *
     * @return
     */
    public String getString() {
        return (String) raw;
    }

    /**
     * Return a boolean value
     *
     * @return
     */
    public Boolean getBoolean() {

        if (raw instanceof Boolean) {
            return (Boolean) raw;
        } else if (raw instanceof String) {
            return Boolean.valueOf((String) raw);
        } else if (raw instanceof Number) {
            return ((Number) raw).longValue() == 1;
        }

        return null;
    }
    
    
    
}
