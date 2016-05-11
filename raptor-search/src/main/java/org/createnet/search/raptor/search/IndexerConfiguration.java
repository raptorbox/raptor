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
package org.createnet.search.raptor.search;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class IndexerConfiguration {
  
  public String type;
  public ElasticSearch elasticsearch;
  
  public class ElasticSearch {

    public ElasticSearch() {
      transport = new Transport();
    }
    
    public String type;
    public Map<String, String> clientConfig = new HashMap();
    public Transport transport;
    public Indices indices;
        
    public class Transport {
      public String host;
      public int port;
    }
    
    public class Indices {
            
      public String source;
      public Map<String, String> definitions = new HashMap();      
      public Map<String, IndexDescriptor> names = new HashMap();      
      
      public class IndexDescriptor {
        public String index; 
        public String type; 
      }      
      
    }
    
  }
  
}
