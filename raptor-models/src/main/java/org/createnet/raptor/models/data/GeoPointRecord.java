/*
 * Copyright 2016 CREATE-NET
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

import org.createnet.raptor.models.objects.RaptorComponent;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class GeoPointRecord extends Record<String> {

    public Point point;
    protected String value;

    public GeoPointRecord() {}
    
    public GeoPointRecord(String point) {
        this.point = new Point(point);
    }
    
    public GeoPointRecord(double latitude, double longitude) {
        this.point = new Point(latitude, longitude);
    }

    @Override
    public String getValue() {
        return point.toString();
    }

    @Override
    public void setValue(Object value)  throws RaptorComponent.ParserException {
        this.value = parseValue(value);
        this.point = new Point(this.value);
    }

    @Override
    public String parseValue(Object raw)  throws RaptorComponent.ParserException {
        return (String)raw;
    }
    
    @Override
    public String getType() {
        return "geo_point";
    }

    public class Point {

        public double latitude;
        public double longitude;

        public Point(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public Point(String val) {

            String[] coords = val.split(",");

            longitude = Double.parseDouble(coords[0].trim());
            latitude = Double.parseDouble(coords[1].trim());
        }

        @Override
        public String toString() {
            return this.longitude + "," + this.latitude;
        }

    }

}
