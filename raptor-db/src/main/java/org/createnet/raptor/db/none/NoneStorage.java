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
package org.createnet.raptor.db.none;

import org.createnet.raptor.db.AbstractStorage;
import org.createnet.raptor.db.config.StorageConfiguration;
import org.createnet.raptor.plugin.PluginConfiguration;
import org.createnet.raptor.plugin.impl.NoPluginConfiguration;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class NoneStorage extends AbstractStorage {

  @Override
  public void connect() {
  }

  @Override
  public void disconnect() {
  }

  @Override
  public void setup(boolean forceSetup) {
  }

  @Override
  public PluginConfiguration<StorageConfiguration> getPluginConfiguration() {
    return new NoPluginConfiguration();
  }


}
