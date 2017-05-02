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
package org.createnet.raptor.models.query;

import com.fasterxml.jackson.databind.JsonNode;
import org.createnet.raptor.models.objects.Device;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
abstract public class BaseQuery implements IQuery, QueryBuilder {


    protected String userId;

    protected Integer offset = 0;
    protected Integer limit = 1000;
    public final SortQuery sortBy = new SortQuery();


    public Integer getOffset() {
        return offset;
    }

    public BaseQuery offset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public BaseQuery limit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public BaseQuery userId(String userId) {
        this.userId = userId;
        return this;
    }

    public JsonNode toJSON() {
        return Device.getMapper().valueToTree(this);
    }
    
    @Override
    public boolean isEmpty() {
        return false;
    }
    
}
