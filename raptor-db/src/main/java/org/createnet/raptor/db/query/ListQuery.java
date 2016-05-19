/*
 * Copyright 2016 Luca Capra <lcapra@create-net.org>.
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

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public interface ListQuery {

  public enum Sort {
    ASC, DESC
  }

  public class SortBy {
    public Sort sortBy;
    public String field;
    public SortBy(String field, Sort sortBy) {
      this.sortBy = sortBy;
      this.field = field;
    }

    public Sort getSortBy() {
      return sortBy;
    }

    public String getSort() {
      return getSortBy().name();
    }
    
    public String getField() {
      return field;
    }
    
  }

  public class QueryParam<T> {

    public String key;
    public T value;
    public String operation = "=";
    
    public QueryParam(String key, T value) {
      this.key = key;
      this.value = value;
    }
    
  }
  
  public class QueryOptions {
    
    public int retries = 3;
    public int timeout = 10;
    public TimeUnit timeoutUnit = TimeUnit.SECONDS;

    public QueryOptions() {
    }
    
  }
  
  public int getOffset();

  public int getLimit();

  public SortBy getSort();

  public List<QueryParam> getParams();
  
  public QueryOptions getQueryOptions();
}
