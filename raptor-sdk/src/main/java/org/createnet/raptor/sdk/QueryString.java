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
package org.createnet.raptor.sdk;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author l
 */
public class QueryString {
    
    public class Pager {
        
        public Integer page;
        public Integer size;
        public String sort;
        public String sortDir = "DESC";
        
        public Map<String, Object> toMap() {
            Map<String, Object> m = new HashMap();
            if (page != null) {
                m.put("page", page);
            }
            if (size != null) {
                m.put("size", size);
            }
            if (sort != null) {
                m.put("sort", sort + "," + sortDir);
            }
            return m;
        }
        
    }
    
    public class Query {
        
        final protected Map<String, Object> m = new HashMap();
        
        public Query add(String field, Object value) {
            m.put(field, value);
            return this;
        }
        
        public Map<String, Object> toMap() {
            return m;
        }

        public Object get(String field) {
            return m.get(field);
        }
        
    }
    
    protected String formatQueryParams(Map<String, Object> params) {
        return params.entrySet().stream()
                .map(p -> p.getKey() + "=" + p.getValue() == null ? "" : p.getValue().toString())
                .reduce((p1, p2) -> p1 + "&" + p2)
                .map(s -> "?" + s)
                .orElse("");
    }
    
    final public Pager pager = new Pager();
    final public Query query = new Query();
    
    @Override
    public String toString() {
        Map<String, Object> m = new HashMap();
        Map p = pager.toMap();
        if (!p.isEmpty()) {
            m.putAll(p);
        }
        Map q = query.toMap();
        if (!q.isEmpty()) {
            m.putAll(q);
        }
        return formatQueryParams(m);
    }
    
}
