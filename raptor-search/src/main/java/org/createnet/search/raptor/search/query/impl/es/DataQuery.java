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
package org.createnet.search.raptor.search.query.impl.es;

import org.createnet.search.raptor.search.query.AbstractQuery;
import java.util.ArrayList;
import java.util.List;
import org.createnet.search.raptor.search.query.Query;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.AndQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class DataQuery extends AbstractQuery {

  public DataQuery() {}
  
  public List<DataQuery> queryList = new ArrayList();

  public boolean timerange = false;
  public boolean numericrange = false;

  public double rangefrom = Double.MIN_VALUE;
  public double rangeto = Double.MAX_VALUE;

  public double timerangefrom = Double.MIN_VALUE;
  public double timerangeto = Double.MAX_VALUE;

  public String numericrangefield;

  public boolean limit = false;
  public int limitcount;

  public boolean geodistance = false;

  public double pointlat;
  public double pointlon;

  public double geodistancevalue;
  public String geodistanceunit = "km";

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

    if (timerange) {
      return;
    }

    if (numericrange && numericrangefield != null) {
      return;
    }

    if (timerange && numericrange && numericrangefield != null
            && (!numericrangefield.contains("lastUpdate"))) {
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

  protected List<QueryBuilder> buildQuery() {
    
    ArrayList<QueryBuilder> queries = new ArrayList();

    if (timerange) {
      RangeQueryBuilder rangeFilter
              = QueryBuilders.rangeQuery("doc.lastUpdate")
              .from((long) timerangefrom).to((long) timerangeto)
              .includeLower(true).includeUpper(true);
      //filter.append(rangeFilter.toString());
      queries.add(rangeFilter);
    }

    if (numericrange) {

      RangeQueryBuilder numericrangeFilter
              = QueryBuilders.rangeQuery("doc." + numericrangefield)
              .from(rangefrom).includeLower(true)
              .to(rangeto).includeUpper(true);

      //filter.append(numericrangeFilter());
      queries.add(numericrangeFilter);
    }

    if (geodistance) {

      GeoDistanceQueryBuilder geodistanceFilter
              = QueryBuilders.geoDistanceQuery("channels.location.current-value")
              .distance(geodistancevalue, DistanceUnit.fromString(geodistanceunit))
              .point(pointlat, pointlon);

      //filter.append(geodistanceFilter.toString());
      queries.add(geodistanceFilter);
    }

    if (geoboundingbox) {

      GeoBoundingBoxQueryBuilder geodbboxFilter
              = QueryBuilders.geoBoundingBoxQuery("channels.location.current-value")
              .topLeft(geoboxupperleftlat, geoboxupperleftlon)
              .bottomRight(geoboxbottomrightlat, geoboxbottomrightlon);

      //filter.append(geodbboxFilter());
      queries.add(geodbboxFilter);
    }

    if (match) {
      TermQueryBuilder matchFilter = QueryBuilders.termQuery(matchfield, matchstring);
      queries.add(matchFilter);
    }
    
    return queries;
  }

  @Override
  public String format() throws QueryException {

    validate();

    List<QueryBuilder> queries = buildQuery();
    
    BoolQueryBuilder qb = QueryBuilders.boolQuery();
    
    for(QueryBuilder qbpart : queries) {
      qb.must(qbpart);
    }
    
    if (!qb.hasClauses()) {
      throw new QueryException("Query is empty");
    }

    return qb.toString();
  }

}
