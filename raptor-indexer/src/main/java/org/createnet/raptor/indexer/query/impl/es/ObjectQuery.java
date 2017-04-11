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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class ObjectQuery extends AbstractESQuery {

    public String search;

    public String name;
    public String description;

    public Map<String, Object> customFields = new HashMap();

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

        if (search != null && search.length() > 0) {
            return;
        }

        if (name != null && name.length() > 0) {
            return;
        }

        if (description != null && description.length() > 0) {
            return;
        }

//    throw new QueryException("Query is empty");
    }

    @Override
    protected QueryBuilder buildQuery() {

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        boolQuery.must(QueryBuilders.matchQuery("userId", userId));

        if (search != null && search.length() > 0) {
            boolQuery.must(QueryBuilders.multiMatchQuery(search, "name", "customFields.*", "description", "id"));
            return boolQuery;
        }

        if (name != null && name.length() > 0) {
            boolQuery.must(QueryBuilders.matchQuery("name", name.toLowerCase()));
        }

        if (description != null && description.length() > 0) {
            boolQuery.must(QueryBuilders.matchQuery("description", description.toLowerCase()));
        }

        if (customFields != null && !customFields.isEmpty()) {
            
            for (Iterator<String> iterator = customFields.keySet().iterator(); iterator.hasNext();) {
                String key = iterator.next();
                Object val = customFields.get(key);
                boolQuery.must(QueryBuilders.matchQuery("customFields." + key, val));
            }

        }

        return boolQuery.hasClauses() ? boolQuery : null;
    }

}
