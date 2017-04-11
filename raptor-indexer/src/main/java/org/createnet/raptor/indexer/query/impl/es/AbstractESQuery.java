/*
 * Copyright 2017 Luca Capra <luca.capra@fbk.eu>.
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
import org.createnet.raptor.indexer.query.AbstractQuery;
import org.elasticsearch.index.query.QueryBuilder;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
abstract public class AbstractESQuery extends AbstractQuery {
    
    abstract QueryBuilder buildQuery();
    
    @JsonIgnore
    protected QueryBuilder getQueryBuilder() throws QueryException {
        QueryBuilder qb = buildQuery();
        if (qb == null) {
            throw new QueryException("Query is empty");
        }
        return qb;
    }
    
    @JsonIgnore
    @Override
    public Object getNativeQuery() throws QueryException {        
        validate();
        return (Object) getQueryBuilder();
    }

    @JsonIgnore
    @Override
    public String format() throws QueryException {
        validate();
        return getQueryBuilder().toString();
    }

}
