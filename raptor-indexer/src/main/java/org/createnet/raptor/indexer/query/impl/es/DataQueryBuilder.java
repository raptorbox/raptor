/*
 * Copyright 2017 Luca Capra <lcapra@fbk.eu>.
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
package org.createnet.raptor.indexer.query.impl.es;

import java.time.Instant;
import org.createnet.raptor.indexer.query.Query;
import org.createnet.raptor.models.data.types.instances.GeoPoint;
import org.createnet.raptor.models.data.types.instances.DistanceUnit;

/**
 * Create prestructured data queries 
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class DataQueryBuilder {

    /**
     * Search for a match on a provided field
     *
     * @param field
     * @param value
     * @return
     */
    public static DataQuery match(Query.Field field, String value) {
        return new DataQuery().setMatch(field, value);
    }
    
    /**
     * Search for result from a specified time until now
     *
     * @param from
     * @return
     */
    public static DataQuery timeRange(Instant from) {
        return new DataQuery().timeRange(from);
    }

    /**
     * Search for result in a time frame range
     *
     * @param from
     * @param to
     * @return
     */
    public static DataQuery timeRange(Instant from, Instant to) {
        return new DataQuery().timeRange(from, to);
    }

    /**
     * Search for result in a geo-spatial bounding box
     *
     * @param nw top-left coordinate
     * @param se bottom-right coordinate
     * @return
     */
    public static DataQuery boundingBox(GeoPoint nw, GeoPoint se) {
        return new DataQuery().boundingBox(nw, se);
    }

    /**
     * Search for result in the radius of a geo-spatial point
     *
     * @param point
     * @param distance
     * @param unit
     * @return
     */
    public static DataQuery distance(GeoPoint point, double distance, DistanceUnit unit) {
        return new DataQuery().distance(point, distance, unit);
    }

    /**
     * Search for results in a numeric range
     *
     * @param field a channel name of type numeric
     * @param from
     * @param to
     * @return
     */
    public static DataQuery range(String field, double from, double to) {
        return new DataQuery().range(field, from, to);
    }

    /**
     * Search for results in a numeric range
     *
     * @param field a channel name of type numeric
     * @param from
     * @param to
     * @return
     */
    public DataQuery range(String field, long from, long to) {
        return new DataQuery().range(field, from, to);
    }

    
}
