/*
 *
 * Copyright 2014 CREATE-NET
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.createnet.servioticy.configmanager.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Properties;

import org.createnet.servioticy.configmanager.exception.ConfigPropertyNotFound;
import org.createnet.servioticy.configmanager.exception.ConfigurationFileNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 *
 * @author Luca Capra <luca.capra@create-net.org>
 */
public class ConfigManager implements IConfigurationManager, Serializable
{

    protected Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    
    protected String _defaultSource = "/etc/servioticy";
    protected String _defaultFile = "config.yml";

    protected String defaultSource;
    protected String defaultFile;

    protected String defaultProperties = "/config.properties";
    protected String defaultConfiguration = "/default.yml";

    static HashMap<String, String> resolvedCache = new HashMap<>();
    
    public static void main(String [] args) throws ConfigPropertyNotFound
    {

        ConfigManager m = new ConfigManager();

//        m.load(Configuration.class);
//        String x = m.get("couchbase.public_uris");

//        System.out.println("Loaded config ----------------------------");
//        System.out.println(x);

    }

    public ConfigManager() {

        Properties props = new Properties();

        try {

            props.load(ConfigManager.class.getResourceAsStream(defaultProperties));

            defaultSource = props.getProperty("source", _defaultSource);
            defaultFile = props.getProperty("file", _defaultFile);

        } catch (Exception ex) {

            logger.warn("Cannot read properties file {}", defaultProperties);

            defaultSource = _defaultSource;
            defaultFile = _defaultFile;

        }

    }

    public IConfiguration load(Class classType, String[] paths) throws ConfigurationFileNotFound
    {

        InputStream input = null;
        File file = null;
        
        for (String path : paths) {
            
            // try to load config specified configuration
            logger.info("Load configuration, checking {}", path);
            
            file = new File(path);
            input = this.tryLoad(file);

            if(input != null) {
                break;
            }

        }

        if(input == null) {
            throw new ConfigurationFileNotFound();
        }
        
        Configuration conf = null;
        try {
            
            Constructor constructor = new Constructor(classType);
            Yaml yaml = new Yaml(constructor);

            conf = (Configuration) yaml.load(input);
            conf.setConfigurationFile(file);

        }
        catch(Exception e) {
            logger.error(e.getMessage());
        }
        
        return conf;
    }

    protected FileInputStream tryLoad(File configFile) {
        try {
            return new FileInputStream(configFile);
        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
        }
        return null;
    }

}
