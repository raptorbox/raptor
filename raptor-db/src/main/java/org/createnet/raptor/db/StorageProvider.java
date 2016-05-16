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
package org.createnet.raptor.db;

import org.createnet.raptor.db.config.StorageConfiguration;
import org.createnet.raptor.db.couchbase.CouchbaseStorage;
import org.createnet.raptor.db.none.NoneStorage;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class StorageProvider extends AbstractStorage {

  private Storage instance;
  
  @Override
  public void initialize(StorageConfiguration configuration) throws StorageException {

    super.initialize(configuration);

    switch (configuration.type) {
      case "couchbase":
        instance = new CouchbaseStorage();
        break;
      case "none":
        instance = new NoneStorage();
        break;
      default:
        throw new StorageException("Storage type `"+ configuration.type +"` is not supported");
    }
    
    instance.initialize(configuration);
  }

  @Override
  public void setup(boolean forceSetup) {
    instance.setup(forceSetup);
  }

  @Override
  public void connect() {
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

}
