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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
abstract public class AbstractQuery implements ListQuery {

  public final List<QueryParam> params = new ArrayList();
  public SortBy sortBy;
  public int offset = 0;
  public int limit = 0;
  
  final private QueryOptions options = new QueryOptions();
  
  @Override
  public int getOffset() {
    return offset;
  }

  @Override
  public int getLimit() {
    return limit;
  }

  @Override
  public SortBy getSort() {
    return sortBy;
  }
  
  @Override
  public List<QueryParam> getParams() {
    return params;
  }
  
  @Override
  public QueryOptions getQueryOptions() {
    return options;
  }
  
  public void setSort(String field, Sort sort) {
    this.sortBy = new SortBy(field, sort);
  }
  
}
