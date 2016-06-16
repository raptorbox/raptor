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
package org.createnet.raptor.plugin;

import java.io.File;
import java.io.IOException;
import org.createnet.raptor.config.Configuration;
import org.createnet.raptor.config.ConfigurationLoader;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class PluginConfigurationLoader extends ConfigurationLoader {

  private static PluginConfigurationLoader instance;

  static public PluginConfigurationLoader getInstance() {
    if (instance == null) {
      instance = new PluginConfigurationLoader();
    }
    return instance;
  }

  protected PluginConfigurationLoader() {
    super();
  }

  @Override
  protected File getFile(String filename) {
    File file = new File(getConfigPath() + File.separator + filename);
    if (file.exists()) {
      logger.debug("Using configuration at {}", file.getAbsolutePath());
      return file;
    }
    return super.getFile(filename);
  }

  public Configuration load(Plugin plugin) {
    return load(plugin.getPluginConfiguration());
  }

  public Configuration load(PluginConfiguration pluginConfiguration) {
    
    if(pluginConfiguration == null || pluginConfiguration.getConfigurationClass() == null) {
      return null;
    }
    
    String name = pluginConfiguration.getName();
    String basePath = pluginConfiguration.getPath();
    Class<? extends Configuration> clazz = pluginConfiguration.getConfigurationClass();

    Configuration config = cache.getOrDefault(name, null);
    if (config == null) {
      String path = basePath != null ? basePath : name;
      try {
        config = mapper.readValue(getFile(path), clazz);
      } catch (IOException ex) {
        logger.error("Failed to read configuration {} ({})", name, path, ex);
        throw new RuntimeException(ex);
      }
      cache.put(name, config);
    }

    return config;
  }

}
