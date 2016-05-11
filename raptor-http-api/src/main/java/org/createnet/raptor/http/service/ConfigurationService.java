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
package org.createnet.raptor.http.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import org.createnet.raptor.http.configuration.AuthConfiguration;
import org.createnet.raptor.http.configuration.Configuration;
import org.createnet.raptor.http.configuration.DispatcherConfiguration;
import org.createnet.raptor.http.configuration.IndexerConfiguration;
import org.createnet.raptor.http.configuration.StorageConfiguration;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */

@Service
@Singleton
public class ConfigurationService {
  
  final private String basePath = "/etc/raptor/";
  final private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
  
  final private Map<String, Configuration> cache = new HashMap();
  
  protected File getFile(String filename) {
    return new File(basePath + filename + ".yml");
  }
  
  protected Configuration getInstance(String name, Class<? extends Configuration> clazz) throws IOException {
    
    Configuration config = cache.get(name);
    if(config == null) {
      Configuration instance = mapper.readValue(getFile(name), clazz);
      config = cache.put(name, instance);
    }
    
    return config;
  }
  
  public StorageConfiguration getStorage() throws IOException {
    return (StorageConfiguration) getInstance("storage", StorageConfiguration.class);
  }
  
  public AuthConfiguration getAuth() throws IOException {
    return (AuthConfiguration) getInstance("auth", AuthConfiguration.class);
  }
  
  public IndexerConfiguration getIndexer() throws IOException {
    return (IndexerConfiguration) getInstance("indexer", IndexerConfiguration.class);
  }
  
  public DispatcherConfiguration getDispatcher() throws IOException {
    return (DispatcherConfiguration) getInstance("dispatcher", DispatcherConfiguration.class);
  }
  
}
