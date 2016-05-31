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
package org.createnet.raptor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ConfigurationLoader {

  protected final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);

  static final private String defaultPath = "/etc/raptor/";
  private File basePathFile;

  public static String getConfigPath() {
    String configDir = System.getProperty("configDir", null);
    return configDir == null ? defaultPath : configDir;
  }

  public String getBasePath() {

    if (basePathFile == null) {
      basePathFile = new File(getConfigPath());
      if (!basePathFile.exists()) {
        throw new RuntimeException("Configuration directory does not exists: " + basePathFile.getPath());
      }
    }

    return basePathFile.getAbsolutePath();
  }
  final private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

  final private Map<String, Configuration> cache = new HashMap();

  protected File getFile(String filename) {
    return new File(getBasePath() + "/" + filename + ".yml");
  }

  public Configuration getInstance(String name, Class<? extends Configuration> clazz) throws ConfigurationException {

    Configuration config = cache.get(name);
    if (config == null) {
      try {
        config = mapper.readValue(getFile(name), clazz);
      } catch (IOException ex) {
        logger.error("Failed to read configuration", ex);
        throw new RuntimeException(ex);
      }
      cache.put(name, config);
    }

    return config;
  }

}
