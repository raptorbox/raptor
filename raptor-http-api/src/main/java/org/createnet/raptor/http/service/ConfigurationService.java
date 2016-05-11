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

import org.createnet.raptor.http.exception.ConfigurationException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */

@Service
@Singleton
public class ConfigurationService {
  
  final private Logger logger = LoggerFactory.getLogger(ConfigurationService.class);
  
  final private String basePath = "/etc/raptor/";
  final private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
  
  final private Map<String, Configuration> cache = new HashMap();
  
  protected File getFile(String filename) {
    return new File(basePath + filename + ".yml");
  }
  
  protected Configuration getInstance(String name, Class<? extends Configuration> clazz) throws ConfigurationException {
    
    Configuration config = cache.get(name);
    if(config == null) {
      try {
        config = mapper.readValue(getFile(name), clazz);
      }
      catch(IOException ex) {
        logger.error("Failed to read configuration", ex);
        throw new RuntimeException(ex);
      }
      cache.put(name, config);
    }
    
    return config;
  }
  
  public StorageConfiguration getStorage() throws ConfigurationException {
    return (StorageConfiguration) getInstance("storage", StorageConfiguration.class);
  }
  
  public AuthConfiguration getAuth() throws ConfigurationException {
    return (AuthConfiguration) getInstance("auth", AuthConfiguration.class);
  }
  
  public IndexerConfiguration getIndexer() throws ConfigurationException {
    return (IndexerConfiguration) getInstance("indexer", IndexerConfiguration.class);
  }
  
  public DispatcherConfiguration getDispatcher() throws ConfigurationException {
    return (DispatcherConfiguration) getInstance("dispatcher", DispatcherConfiguration.class);
  }
  
}
