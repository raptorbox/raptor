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
package org.createnet.raptor.search.query;

import com.fasterxml.jackson.databind.JsonNode;
import org.createnet.raptor.search.Indexer;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public abstract class AbstractQuery implements Query {

    private String index;
    private String type;

    private Integer limit = null;
    private Integer offset = null;

    private Query.SortBy sort;

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getIndex() {
        return index;
    }

    @Override
    public Integer getOffset() {
        return offset;
    }

    @Override
    public Integer getLimit() {
        return limit;
    }

    @Override
    public SortBy getSort() {
        return sort;
    }

    @Override
    public void setIndex(String index) {
        this.index = index;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public void setSort(SortBy sort) {
        this.sort = sort;
    }

    @Override
    public JsonNode toJSON() throws QueryException {
        return Indexer.getObjectMapper().convertValue(this, JsonNode.class);
    }
    
}
