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

import javax.inject.Singleton;
import org.createnet.raptor.config.ConfigurationLoader;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.createnet.raptor.http.configuration.AuthConfiguration;
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
public class ConfigurationService extends ConfigurationLoader {
  
  final private Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

  public StorageConfiguration getStorage()  {
    return (StorageConfiguration) getInstance("storage", StorageConfiguration.class);
  }
  
  public AuthConfiguration getAuth()  {
    return (AuthConfiguration) getInstance("auth", AuthConfiguration.class);
  }
  
  public IndexerConfiguration getIndexer() {
    return (IndexerConfiguration) getInstance("indexer", IndexerConfiguration.class);
  }
  
  public DispatcherConfiguration getDispatcher() {
    return (DispatcherConfiguration) getInstance("dispatcher", DispatcherConfiguration.class);
  }
  
}
