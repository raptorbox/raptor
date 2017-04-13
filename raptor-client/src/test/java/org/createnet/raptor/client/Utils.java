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
package org.createnet.raptor.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import org.createnet.raptor.models.objects.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class Utils {

    static final Logger log = LoggerFactory.getLogger(Utils.class);
    static final String settingsFile = "settings.properties";
    static Raptor instance;

    static public Properties loadSettings() {

        ClassLoader classLoader = Utils.class.getClassLoader();
        File file = new File(classLoader.getResource(settingsFile).getFile());
        
        InputStream input;
        try {
            input = new FileInputStream(file);
            Properties prop = new Properties();
            prop.load(input);
            return prop;
            
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    static public Raptor getRaptor() {
        Properties prop = Utils.loadSettings();
        if (instance == null) {
            instance = new Raptor(prop.getProperty("url"), prop.getProperty("username"), prop.getProperty("password"));
            log.debug("Performing login");
            instance.Auth.login();
            log.debug("Logged in");
        }
        return instance;
    }
    
    static public Device createDevice(String name) throws IOException {
        Device d = new Device();
        d.name = name;
        return getRaptor().Device.create(d);
    }
    
    static public void waitFor(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
