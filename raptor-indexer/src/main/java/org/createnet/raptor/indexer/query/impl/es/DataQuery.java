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
package org.createnet.raptor.indexer.query.impl.es;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.createnet.raptor.indexer.query.Query;
import org.createnet.raptor.models.data.types.instances.GeoPoint;
import org.createnet.raptor.models.data.types.instances.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class DataQuery extends AbstractESQuery {

    public DataQuery() {
    }

    @JsonIgnore
    private String userId;

    @JsonIgnore
    private String objectId;

    @JsonIgnore
    private String streamId;

    public List<DataQuery> queryList = new ArrayList();

    public boolean timerange = false;
    public boolean numericrange = false;

    public double numericrangefrom = Double.MIN_VALUE;
    public double numericrangeto = Double.MAX_VALUE;

    public double timerangefrom = Double.MIN_VALUE;
    public double timerangeto = Double.MAX_VALUE;

    public String numericrangefield;

    public boolean limit = false;
    public int limitcount;

    public boolean geodistance = false;

    public double pointlat;
    public double pointlon;

    public double geodistancevalue;
    public DistanceUnit geodistanceunit = DistanceUnit.kilometers;

    public boolean geoboundingbox = false;

    public double geoboxupperleftlat;
    public double geoboxupperleftlon;
    public double geoboxbottomrightlat;
    public double geoboxbottomrightlon;

    public boolean match = false;
    public String matchfield;
    public String matchstring;

    @Override
    public void validate() throws Query.QueryException {

        if (getUserId() == null) {
            throw new QueryException("userId is missing");
        }

        if (getObjectId() == null) {
            throw new QueryException("objectId is missing");
        }

        if (getStreamId() == null) {
            throw new QueryException("streamId is missing");
        }

        if (timerange) {
            return;
        }

        if (numericrange && numericrangefield != null) {
            return;
        }

        if (timerange && numericrange && numericrangefield != null
                && (!numericrangefield.contains("timestamp"))) {
            return;
        }

        if (geodistance ^ geoboundingbox) {
            return;
        }

        if (match && (matchfield != null && matchstring != null)) {
            return;
        }

        throw new Query.QueryException("Query is empty");
    }
    
    private org.elasticsearch.common.unit.DistanceUnit getDistanceUnit(DistanceUnit unit) {
        switch(unit) {
            case centimeters:
                return org.elasticsearch.common.unit.DistanceUnit.CENTIMETERS;
            case feets:
                return org.elasticsearch.common.unit.DistanceUnit.FEET;
            case inches:
                return org.elasticsearch.common.unit.DistanceUnit.INCH;
            case kilometers:
                return org.elasticsearch.common.unit.DistanceUnit.KILOMETERS;
            case meters:
                return org.elasticsearch.common.unit.DistanceUnit.METERS;
            case miles:
                return org.elasticsearch.common.unit.DistanceUnit.MILES;
            case millimeters:
                return org.elasticsearch.common.unit.DistanceUnit.MILLIMETERS;
            case nauticalMiles:
                return org.elasticsearch.common.unit.DistanceUnit.NAUTICALMILES;
            case yards:
                return org.elasticsearch.common.unit.DistanceUnit.YARD;
            default:
                throw new QueryException("Unit not supported " + unit.name());
        }
        
        
    }
    
    @Override
    protected QueryBuilder buildQuery() {

        ArrayList<QueryBuilder> queries = new ArrayList();
        ArrayList<QueryBuilder> filters = new ArrayList();

        if (timerange) {
            RangeQueryBuilder rangeFilter
                    = QueryBuilders.rangeQuery("timestamp")
                            .from((long) timerangefrom).to((long) timerangeto)
                            .includeLower(true).includeUpper(true);
            //filter.append(rangeFilter.toString());
            queries.add(rangeFilter);
        }

        if (numericrange) {
            RangeQueryBuilder numericrangeFilter
                    = QueryBuilders.rangeQuery("channels." + numericrangefield)
                            .from(numericrangefrom).includeLower(true)
                            .to(numericrangeto).includeUpper(true);

            //filter.append(numericrangeFilter());
            queries.add(numericrangeFilter);
        }

        if (geodistance) {
            GeoDistanceQueryBuilder geodistanceFilter = QueryBuilders.geoDistanceQuery("channels.location")
                    .distance(geodistancevalue, getDistanceUnit(this.geodistanceunit))
                    .point(pointlat, pointlon);

            filters.add(geodistanceFilter);
        }

        if (geoboundingbox) {
            GeoBoundingBoxQueryBuilder geodbboxFilter = QueryBuilders.geoBoundingBoxQuery("channels.location");

            geodbboxFilter.topLeft().reset(geoboxupperleftlat, geoboxupperleftlon);
            geodbboxFilter.bottomRight().reset(geoboxbottomrightlat, geoboxbottomrightlon);

            filters.add(geodbboxFilter);
        }

        if (match) {
            MatchQueryBuilder matchFilter = QueryBuilders.matchQuery(matchfield, matchstring);
            queries.add(matchFilter);
        }

        queries.add(QueryBuilders.matchQuery("userId", getUserId()));
        queries.add(QueryBuilders.matchQuery("objectId", getObjectId()));
        queries.add(QueryBuilders.matchQuery("streamId", getStreamId()));

        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        queries.forEach((qbpart) -> {
            qb.must(qbpart);
        });

        // add geo-spatial filter
        if (!filters.isEmpty()) {
            filters.forEach((qbpart) -> {
                qb.filter(qbpart);
            });
        }

        return qb;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    /**
     * Search for a match on a specified channel
     *
     * @param field
     * @param value
     * @return
     */
    public DataQuery setMatch(String field, String value) {
        match = true;
        matchfield = field;
        matchstring = value;
        return this;
    }

    /**
     * Search for a match on a provided field
     *
     * @param field
     * @param value
     * @return
     */
    public DataQuery setMatch(Field field, String value) {
        match = true;
        matchfield = field.name();
        matchstring = value;
        return this;
    }    
    
    /**
     * Search for result from a specified time until now
     *
     * @param from
     * @return
     */
    public DataQuery timeRange(Instant from) {
        return timeRange(from, Instant.now());
    }

    /**
     * Search for result in a time frame range
     *
     * @param from
     * @param to
     * @return
     */
    public DataQuery timeRange(Instant from, Instant to) {
        this.timerange = true;
        this.timerangefrom = from.getEpochSecond();
        this.timerangeto = to.getEpochSecond();
        return this;
    }

    /**
     * Search for result in a geo-spatial bounding box
     *
     * @param nw top-left coordinate
     * @param se bottom-right coordinate
     * @return
     */
    public DataQuery boundingBox(GeoPoint nw, GeoPoint se) {
        this.geoboundingbox = true;
        this.geoboxupperleftlat = nw.getLat();
        this.geoboxupperleftlon = nw.getLon();
        this.geoboxbottomrightlon = se.getLon();
        this.geoboxbottomrightlat = se.getLat();
        return this;
    }

    /**
     * Search for result in the radius of a geo-spatial point
     *
     * @return
     */
    public DataQuery distance(GeoPoint point, double distance, DistanceUnit unit) {
        this.geodistance = true;
        this.geodistanceunit = unit;
        this.geodistancevalue = distance;
        this.pointlat = point.getLat();
        this.pointlon = point.getLon();
        return this;
    }

    /**
     * Search for results in a numeric range
     *
     * @param field a channel name of type numeric
     * @param from
     * @param to
     * @return
     */
    public DataQuery range(String field, double from, double to) {
        this.numericrange = true;
        this.numericrangefrom = from;
        this.numericrangeto = to;
        this.numericrangefield = field;
        return this;
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
        this.numericrange = true;
        this.numericrangefrom = new Long(from).doubleValue();
        this.numericrangeto = new Long(to).doubleValue();
        this.numericrangefield = field;
        return this;
    }

}
