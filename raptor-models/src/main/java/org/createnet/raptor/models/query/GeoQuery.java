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

import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class GeoQuery implements IQuery {

    static public class Distance {

        public GeoJsonPoint center = null;
        public Double radius = null;
        public Metrics unit = Metrics.KILOMETERS;
    }

    static public class BoundingBox {

        public GeoJsonPoint northWest = null;
        public GeoJsonPoint southWest = null;
    }

    protected final Distance distance = new Distance();
    protected final BoundingBox boundingBox = new BoundingBox();

    public GeoQuery distance(GeoJsonPoint center, double radius, Metrics unit) {
        distance.center = center;
        distance.radius = radius;
        distance.unit = unit;
        return this;
    }

    public GeoQuery boundingBox(GeoJsonPoint nw, GeoJsonPoint sw) {
        this.boundingBox.northWest = nw;
        this.boundingBox.southWest = sw;
        return this;
    }

    @Override
    public boolean isEmpty() {
        return ((getDistance().radius == null || getDistance().center == null)
                && (getBoundingBox().northWest == null || getBoundingBox().southWest == null));
    }

    public Distance getDistance() {
        return distance;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

}
