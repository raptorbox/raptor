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
package org.createnet.raptor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ConfigurationLoader {

    protected final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);

    static final ConfigurationLoader instance = new ConfigurationLoader();

    static final private String raptorPath = "/etc/raptor/";
    static private String defaultPath;

    private String configPath;
    private File configPathFile;

    public ConfigurationLoader() {
        String configDir = System.getProperty("configDir", null);
        defaultPath = configDir == null ? raptorPath : configDir;
        logger.debug("Default configuration directory set to {}", defaultPath);
    }

    public String getConfigPath() {
        return configPath == null ? defaultPath : configPath;
    }

    public void setConfigPath(String configPath) {
        logger.debug("Configuration directory set to {}", configPath);
        cache.clear();
        this.configPath = configPath;
    }

    final private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    final private Map<String, Configuration> cache = new HashMap();

    protected File getFile(String filename) {
        return Paths.get(getConfigPath(), filename + ".yml").toFile();
    }

    public Configuration loadConfiguration(String name, Class<? extends Configuration> clazz) {

        Configuration config = cache.get(name);
        if (config == null) {
            logger.debug("Parsing configuration file `{}`", name);
            try {
                File configFile = getFile(name);
                if(!configFile.exists()) {
                    logger.error("configuration file `{}` does not exists", name);
                    throw new IOException("File does not exists: " + configFile.getAbsolutePath());
                }
                config = mapper.readValue(configFile, clazz);
            } catch (IOException ex) {
                logger.error("Failed to read configuration: {}", ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
            cache.put(name, config);
        }

        return config;
    }

    public static Configuration getConfiguration(String name, Class<? extends Configuration> clazz) {
        return getLoader().loadConfiguration(name, clazz);
    }

    public static ConfigurationLoader getLoader() {
        return instance;
    }

}
