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
package org.createnet.raptor.search.raptor.search.query.impl.es;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.createnet.raptor.search.raptor.search.query.AbstractQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class TreeQuery extends AbstractQuery {

    enum TreeQueryType {
        Parent, Children, Root
    }

    public String id;
    public String parentId;
    public TreeQueryType queryType;

    @JsonIgnore
    private String userId;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    @Override
    public void validate() throws QueryException {

        if (userId == null) {
            throw new QueryException("userId not specified");
        }

    }

    protected QueryBuilder buildQuery() {

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        boolQuery.must(QueryBuilders.matchQuery("userId", userId));

        switch (queryType) {
            case Parent:
                break;
            case Children:
                break;
            case Root:
                break;
        }

//    if (name != null && name.length() > 0) {
//      boolQuery.must(QueryBuilders.matchQuery("name", name.toLowerCase()));
//    }
//
//    if (description != null && description.length() > 0) {
//      boolQuery.must(QueryBuilders.matchQuery("description", description.toLowerCase()));
//    }
//
//    if (customFields != null && !customFields.isEmpty()) {
//
//      Iterator<?> keys = customFields.entrySet().iterator();
//      while (keys.hasNext()) {
//
//        String key = (String) keys.next();
//        String val = customFields.get(key).toString().toLowerCase();
//
//        boolQuery.must(QueryBuilders.matchQuery("customFields." + key, val));
//
//      }
//
//    }
//
//    return boolQuery.hasClauses() ? boolQuery : null;
        return null;
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
