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
package org.createnet.raptor.sdk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import org.createnet.raptor.models.auth.Role;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.sdk.config.Config;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class Utils {

    static private final int socketTimeout = 120*1000;
    static private final int reqTimeout = 120*1000;
    
    static final Logger log = LoggerFactory.getLogger(Utils.class);
    static final String settingsFile = "settings.properties";
    static Raptor instance;

    public interface Function {
        public boolean call();
    }

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
            instance.getClient().configureTimeout(socketTimeout, reqTimeout);
            log.debug("Performing login for {}", prop.getProperty("username"));
            instance.Auth().login();
            log.debug("Logged in");
        }
        return instance;
    }

    static public Device createDevice(String name) throws IOException {
        Device d = new Device();
        d.name(name);
        return getRaptor().Inventory().create(d);
    }

    static public void waitFor(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    static public void waitUntil(int repeat, Function callback) {
        int count = 0;
        while (callback.call()) {
            Utils.waitFor(1000);
            count++;
            Assert.assertTrue(count <= repeat);            
            log.debug("Waiting {} of {} seconds", count, repeat);
        }
    }

    static public Device createDevice(Device d) {
        Device d1 = getRaptor().Inventory().create(d);
        return d1;
    }

    static public Device createDevice(Raptor r, Device d) {
        Device d1 = r.Inventory().create(d);
        return d1;
    }

    /**
     * Create a new user and initialize a raptor instance
     *
     * @param username
     * @param roles
     * @return
     */
    static public Raptor createNewInstance(String username, Set<Role> roles) {

        User user = getRaptor().Admin().User().create(username, username + Math.random(), username + "@test.raptor.local", roles);

        assert user != null;
        assert getRaptor().Auth().getToken() != null;

        Raptor r = new Raptor(new Config(instance.getConfig().getUrl(), username, username));
        r.getClient().configureTimeout(socketTimeout, reqTimeout);
        r.Auth().login();

        return r;
    }

    /**
     * Create a new admin user and initialize a raptor instance
     *
     * @param username
     * @return
     */
    static public Raptor createNewInstance(String username) {

        String password = username + Math.random();
        User user = getRaptor().Admin().User().createAdmin(username, password, username + "@test.raptor.local");
        log.debug("Created user {} : {} with uuid {}", username, password, user.getUuid());
        assert user != null;

        Raptor r = new Raptor(new Config(instance.getConfig().getUrl(), username, password));
        r.getClient().configureTimeout(socketTimeout, reqTimeout);
        r.Auth().login();

        return r;
    }

    /**
     * Create a new admin user and initialize a raptor instance
     *
     * @return
     */
    static public Raptor createNewInstance() {

        String username = rndUsername();
        String password = username + Math.random();

        User user = getRaptor().Admin().User().createAdmin(username, password, username + "@test.raptor.local");
        log.debug("Created user {} : {} with uuid {}", username, password, user.getUuid());

        assert user != null;

        Raptor r = new Raptor(new Config(instance.getConfig().getUrl(), username, password));
        r.getClient().configureTimeout(socketTimeout, reqTimeout);
        r.Auth().login();

        return r;
    }
    
    static public Raptor createNewUserInstance() {

        String username = rndUsername();
        String password = username + Math.random();

        User user = getRaptor().Admin().User().create(username, password, username + "@test.raptor.local");
        log.debug("Created user {} : {} with uuid {}", username, password, user.getUuid());

        assert user != null;

        Raptor r = new Raptor(new Config(instance.getConfig().getUrl(), username, password));
        r.getClient().configureTimeout(socketTimeout, reqTimeout);
        r.Auth().login();

        return r;
    }

    public static String rndUsername() {
        int rnd = ((int) (Math.random() * 100000000)) + (int) System.currentTimeMillis();
        return (rnd % 2 == 0 ? "test_fil_" : "user_ippo_") + rnd;
    }

}
