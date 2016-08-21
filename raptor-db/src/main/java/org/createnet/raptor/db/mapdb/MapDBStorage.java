/*
 * Copyright 2016 CREATE-NET.
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
package org.createnet.raptor.db.mapdb;

import java.io.File;
import java.util.concurrent.ConcurrentMap;
import org.mapdb.*;
import org.createnet.raptor.db.AbstractStorage;
import org.createnet.raptor.db.Storage;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class MapDBStorage extends AbstractStorage {

  protected DB db;
  
  @Override
  public void setup(boolean forceSetup) throws StorageException {
    ConcurrentMap map = db.hashMap("map").createOrOpen();
    map.put("something", "here");
  }

  @Override
  public void connect() throws StorageException {
       
    if (config.mapdb.storage.equals("file")) {
      
      File dir = new File(config.mapdb.storePath);
      if(!dir.exists()) {
        if(!dir.mkdirs()) {
          throw new StorageException("Cannot create directory " + config.mapdb.storePath);
        }
      }
      
      for (ConnectionId connId : Storage.ConnectionId.values()) {       
        File file = new File(config.mapdb.storePath + File.separator + connId.name());
        logger.debug("Create store for {} at {}", connId, file.getPath());
        db = DBMaker.fileDB(file).make();
        addConnection(new MapDBConnection(connId));
      }
      
    } else {
      db = DBMaker.memoryDB().make();
    }
  }

  @Override
  public void disconnect() {
    super.disconnect();
    db.close();
  }
  
}
