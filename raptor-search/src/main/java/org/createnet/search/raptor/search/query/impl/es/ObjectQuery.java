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
import java.util.Iterator;
import java.util.Map;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ObjectQuery extends AbstractQuery {

  public String search;

  public String name;
  public String description;

  public Map<String, Object> customFields;
  
  @Override
  public void validate() throws QueryException {

    if (search != null && search.length() > 0) {
      return;
    }

    if (name != null && name.length() > 0) {
      return;
    }

    if (description != null && description.length() > 0) {
      return;
    }

    if (customFields != null) {
      if (customFields == null || customFields.isEmpty()) {
        throw new QueryException("customFields must be a non-empty object.");
      }
      return;
    }

    throw new QueryException("Query is empty");
  }

  protected QueryBuilder buildQuery() {

    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

    if (search != null && search.length() > 0) {
      boolQuery.must(QueryBuilders.queryStringQuery(search));
      return boolQuery;
    }

    if (name != null && name.length() > 0) {
      boolQuery.must(QueryBuilders.wildcardQuery("name", name.toLowerCase()));
    }

    if (description != null && description.length() > 0) {
      boolQuery.must(QueryBuilders.wildcardQuery("description", description.toLowerCase()));
    }

    if (customFields != null && !customFields.isEmpty()) {

      Iterator<?> keys = customFields.entrySet().iterator();
      while (keys.hasNext()) {

        String key = (String) keys.next();
        String val = customFields.get(key).toString().toLowerCase();

        boolQuery.must(QueryBuilders.matchQuery("customFields." + key, val));

      }

    }

    return boolQuery.hasClauses() ? boolQuery : null;
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
