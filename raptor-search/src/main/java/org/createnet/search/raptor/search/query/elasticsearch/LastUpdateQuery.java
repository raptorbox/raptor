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
package org.createnet.search.raptor.search.query.elasticsearch;

import org.createnet.search.raptor.search.query.AbstractQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class LastUpdateQuery extends AbstractQuery {
  
  private final String objectId;
  private final String streamName;

  public LastUpdateQuery(String objectId, String streamName) {
    this.objectId = objectId;
    this.streamName = streamName;
  }
  
  @Override
  public void validate() throws QueryException {
//    throw new QueryException("Query is empty");
  }

  protected QueryBuilder buildQuery() {
    
    BoolQueryBuilder query = QueryBuilders.boolQuery();
    
    query.must(QueryBuilders.termQuery("objectId", objectId));
    query.must(QueryBuilders.termQuery("stream", streamName));
    
//    FieldSortBuilder sort = SortBuilders.fieldSort("lastUpdate");
//    sort.order(SortOrder.DESC);
  
    query.filter(QueryBuilders.matchAllQuery());
    
    return query.hasClauses() ? query : null;
  }

  @Override
  public String format() throws QueryException {

    validate();

    QueryBuilder qb = buildQuery();

    if (qb == null) {
      throw new QueryException("Query is empty");
    }

    return qb.toString();
  }

}
