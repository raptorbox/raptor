/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.plugin;

import java.io.File;
import java.io.IOException;
import org.createnet.raptor.config.Configuration;
import org.createnet.raptor.config.ConfigurationLoader;
import org.createnet.raptor.config.exception.ConfigurationException;

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
    File file = new File(filename);
    if (file.exists()) {
      return file;
    }
    return super.getFile(filename);
  }

  public Configuration load(Plugin plugin) {
    return load(plugin.getPluginConfiguration());
  }

  public Configuration load(PluginConfiguration pluginConfiguration) {
    
    if(pluginConfiguration == null || pluginConfiguration.getType() == null) {
      return null;
    }
    
    String name = pluginConfiguration.getName();
    String basePath = pluginConfiguration.getPath();
    Class<? extends Configuration> clazz = pluginConfiguration.getType();

    Configuration config = cache.getOrDefault(name, null);
    if (config == null) {
      String path = (basePath == null ? "" : basePath) + name;
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
