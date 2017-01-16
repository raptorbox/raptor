/*
 * Copyright 2017 l.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.createnet.raptor.indexer.query.AbstractQuery;
import org.createnet.raptor.indexer.query.Query;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 *
 * @author l
 */
public class ObjectListQuery extends AbstractESQuery {

//    public String userId;
    final public List<String> ids = new ArrayList();

    @Override
    public void validate() throws Query.QueryException {
    }

    @Override
    protected QueryBuilder buildQuery() {

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        IdsQueryBuilder idsQuery = QueryBuilders.idsQuery(this.getType());
        idsQuery.addIds(ids.stream().toArray(String[]::new));

        boolQuery.must(idsQuery);

        return boolQuery.hasClauses() ? boolQuery : null;
    }

}
