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
package org.createnet.raptor.cli;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.http.ApplicationConfig;
import org.createnet.raptor.http.exception.ConfigurationException;
import org.createnet.search.raptor.search.Indexer;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class Cli {

  static final private Logger logger = LoggerFactory.getLogger(Cli.class);
  final private Commands commands = new Commands();

  public static class CommandName {

    final public static String CLI_NAME = "raptor-cli";

    final public static String SETUP = "setup";
    final public static String INDEX = "index";
    final public static String LAUNCH = "launch";

  }

  public Cli() {

    ServiceLocatorFactory locatorFactory = ServiceLocatorFactory.getInstance();
    ServiceLocator serviceLocator = locatorFactory.create("CliLocator");
    ServiceLocatorUtilities.bind(serviceLocator, new ApplicationConfig.AppBinder());

    serviceLocator.inject(commands);   
    
  }

  public static void main(String[] args) throws ParseException {

    final Cli app = new Cli();
    
    try {
      if(!Cli.isRoot()) {
        logger.warn("Current user is not root");
      }
    }
    catch(Exception e) {
      logger.error("Cannot get current username", e);
    }
    
    Options options = new Options();

    Option setupCommand = Option.builder(CommandName.SETUP)
            .hasArg(false)
            .desc("Setup the current Raptor instance WARNING: if used with force flag, ALL DATA AND INDEXES will be wiped")
            .build();

    Option launchCommand = Option.builder(CommandName.LAUNCH)
            .hasArg(false)
            .desc("Launch a Raptor instance")
            .build();    
    
    options.addOption(setupCommand);
    options.addOption(launchCommand);
    options.addOption("force", false, "Force command execution");

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(CommandName.CLI_NAME, options);

    if (cmd.hasOption("setup")) {
      app.setup(cmd.hasOption("force"));
    }
    if (cmd.hasOption("launch")) {
      app.launch();
    }

  }

  public void setup(boolean force) {
    logger.debug("Running setup, force {}", force);
    try {
      commands.setup(force);
    } catch (Indexer.IndexerException | Storage.StorageException | ConfigurationException ex) {
      logger.error("Error during setup", ex);
    }
  }

  public void launch() {
    logger.debug("Launching new Raptor instance");
    commands.launch();
  }

  // from http://stackoverflow.com/a/24064448/833499 
  public static boolean isRoot() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {

    String osName = System.getProperty("os.name").toLowerCase();
    String className = null;

    if (osName.contains("windows")) {
      className = "com.sun.security.auth.module.NTSystem";
    } else if (osName.contains("linux")) {
      className = "com.sun.security.auth.module.UnixSystem";
    } else if (osName.contains("solaris") || osName.contains("sunos")) {
      className = "com.sun.security.auth.module.SolarisSystem";
    }

    if (className != null) {
      Class<?> c = Class.forName(className);
      Method method = c.getDeclaredMethod("getUsername");
      Object o = c.newInstance();
      String name = (String) method.invoke(o);
      
      return name.equals("root") || name.equals("Administrator");
    }
    
    return false;
  }

}
