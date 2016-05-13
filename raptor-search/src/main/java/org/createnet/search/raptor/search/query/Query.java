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
package org.createnet.search.raptor.search.query;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public interface Query {
 
  public class QueryException extends Exception {

    public QueryException(String m) {
      super(m);
    }
  }
  
  public enum Sort {
    ASC, DESC
  }
  
  public class SortBy {

    public SortBy(String field, Sort sort) {
      this.sort = sort;
      this.field = field;
    }
    
    public Sort sort;
    public String field;
  }  
    
  public String getIndex();
  public String getType();
  
  public SortBy getSort();
  public Integer getLimit();
  public Integer getOffset();
  

  public void validate() throws QueryException;
  public String format() throws QueryException;
  
}
