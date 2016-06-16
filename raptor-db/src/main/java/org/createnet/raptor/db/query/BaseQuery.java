/*
 * Copyright 2016 CREATE-NET http://create-net.org
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
package org.createnet.raptor.db.query;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class BaseQuery extends AbstractQuery {
  
  public static BaseQuery createQuery(QueryParam[] params) {
    
    BaseQuery query = new BaseQuery();
    
    for(QueryParam param : params) {
      query.getParams().add(param);
    }
    
    return query;
  }
  
  public static BaseQuery createQuery(QueryParam[] params, SortBy sortBy) {
    BaseQuery query = (BaseQuery) createQuery(params);
    query.sortBy = sortBy;
    return query;
  }
  
  public static BaseQuery queryBy(String key, String value) {
    BaseQuery query = (BaseQuery) createQuery(new QueryParam[] {
      new QueryParam(key, value)
    });
    return query;
  }
  
  public static BaseQuery queryBy(String key1, String value1, String key2, String value2) {
    BaseQuery query = (BaseQuery) createQuery(new QueryParam[] {
      new QueryParam(key1, value1),
      new QueryParam(key2, value2),
    });
    return query;
  }
  
  public static BaseQuery queryBy(QueryParam... params) {
    return createQuery(params);
  }
  
}
