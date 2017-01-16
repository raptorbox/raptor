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
package org.createnet.raptor.indexer;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import org.createnet.raptor.config.Configuration;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class IndexerConfiguration implements Configuration {

    public IndexerConfiguration() {
    }
    
    public Integer recordFetchLimit = 1000;
    
    public String type;
    public ElasticSearch elasticsearch = new ElasticSearch();

    static public class ElasticSearch {

        public ElasticSearch() {
        }

        public String type;
        public Map<String, String> clientConfig = new HashMap();
        public Transport transport = new Transport();
        public Indices indices = new Indices();

        static public class Transport {

            public Transport() {
            }

            public String host;
            public int port;
        }

        static public class Indices {

            public Indices() {
            }

            public String source;
            public Map<String, JsonNode> definitions = new HashMap();
            public Map<String, IndexDescriptor> names = new HashMap();

            static public class IndexDescriptor {

                public IndexDescriptor() {
                }

                public String index;
                public String type;
            }

        }

    }

}
