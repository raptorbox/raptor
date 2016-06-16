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
package org.createnet.raptor.plugin;

import java.util.Iterator;
import java.util.ServiceLoader;
import org.createnet.raptor.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class PluginLoader<T extends Plugin> {
  
  protected final Logger logger = LoggerFactory.getLogger(PluginLoader.class);
  protected final PluginConfigurationLoader pluginConfigLoader = PluginConfigurationLoader.getInstance();
  
  public T load(String name, Class<T> type) {

    Iterator<T> services = ServiceLoader.load(type).iterator();

    while (services.hasNext()) {
      T service = services.next();
      if (service.getPluginConfiguration() != null
              && service.getPluginConfiguration().getName().equals(name)) {
        logger.debug("Loaded Storage plugin: {}", service.getClass().getName());        
        initialize(service);
        return service;
      }
    }

    throw new RuntimeException("Cannot load plugin " + name + " of type " + type.getName() + "");
  }  
  
  protected void initialize(T service) {
  
 
    Configuration config = pluginConfigLoader.load(service);
    
    if (config == null) {
      service.initialize(null);
      return;
    }
    
    Class<? extends Configuration> configClass = service.getPluginConfiguration().getConfigurationClass();
    service.initialize(configClass.cast(config));
    
  }
  
}
