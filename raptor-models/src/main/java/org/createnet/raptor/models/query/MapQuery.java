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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MapQuery {
    
    protected String containsKey;
    protected Object containsValue;
    protected final Map<String, Object> has = new HashMap();
    
    public MapQuery containsKey(String key) {
        containsKey = key;
        return this;
    }
    
    public MapQuery containsValue(Object val) {
        containsValue = val;
        return this;
    }
    
    public MapQuery has(String key, Object val) {
        has.put(key, val);
        return this;
    }
    
    public MapQuery has(Map<String, Object> values) {
        has.putAll(values);
        return this;
    }

    public String getContainsKey() {
        return containsKey;
    }

    public Object getContainsValue() {
        return containsValue;
    }

    public Map<String, Object> getHas() {
        return has;
    }
    
    @JsonIgnore
    public boolean isEmpty() {
        return (
            getContainsKey() == null &&
            getContainsValue() == null &&
            getHas().isEmpty()
        );
    }
    
}
