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
package org.createnet.raptor.db;

import org.createnet.raptor.db.config.StorageConfiguration;
import org.createnet.raptor.plugin.PluginConfiguration;
import org.createnet.raptor.plugin.PluginLoader;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class StorageProvider extends AbstractStorage {

  private Storage instance;
  protected final PluginLoader<Storage> pluginLoader = new PluginLoader();
  
  @Override
  public void initialize(StorageConfiguration configuration) {
    super.initialize(configuration);
    instance = pluginLoader.load(configuration.type, Storage.class);
  }

  @Override
  public void setup(boolean forceSetup) throws StorageException {
    instance.setup(forceSetup);
  }

  @Override
  public void connect() throws StorageException {
    instance.connect();
  }

  @Override
  public void disconnect() {
    instance.disconnect();
  }

  @Override
  public Connection getConnection(String id) {
    return instance.getConnection(id);
  }

  @Override
  public void destroy() {
    instance.destroy();
  }

  @Override
  public PluginConfiguration<StorageConfiguration> getPluginConfiguration() {
    return null;
  }

}
