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

import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.db.config.StorageConfiguration;
import org.createnet.raptor.db.AbstractConnection;
import org.createnet.raptor.db.query.ListQuery;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
class MapDBConnection extends AbstractConnection {

  private final Logger logger = LoggerFactory.getLogger(MapDBConnection.class);

  private DB db;
  private HTreeMap<String, String> map;
//  private BTreeMap<Long, String> ttl;

  static public class Record {

    public long ttl;
    public JsonNode content;
    public String id;

    private Record() {}
    
    private Record(String id, JsonNode content, long ttl) {
      this.id = id;
      this.content = content;
      this.ttl = ttl;
    }

    public JsonNode toJsonNode() {
      return Storage.mapper.convertValue(this, JsonNode.class);
    }

    @Override
    public String toString() {
      return toJsonNode().toString();
    }

  }

  public MapDBConnection(Storage.ConnectionId connId) {
    this.id = connId.name();
  }

  @Override
  public void setup(boolean forceSetup) throws Storage.StorageException {
    
    if (db != null) {
      return;
    }
    
    if (configuration.mapdb.storage.equals("memory")) {
      db = DBMaker.memoryDB().make();
    } else {

      File file = new File(configuration.mapdb.storePath + File.separator + this.id + ".mapdb");

      if (forceSetup && file.exists()) {
        file.delete();
      }

      try {

        db = DBMaker
              .fileDB(file)
              .closeOnJvmShutdown()
              .closeOnJvmShutdownWeakReference()
  //            .checksumStoreEnable()
  //            .checksumHeaderBypass()
              .transactionEnable()
              .make();
        
        setupIndexes();
        
      }
      catch(Exception e) {
        logger.error("Failed to open db at {}: {}", file.getPath(), e.getMessage());
        throw new Storage.StorageException(e);
      }
      
      logger.debug("Loaded store for {} at {}", this.id, file.getPath());
    }

  }

  @Override
  public void connect() throws Storage.StorageException {

    map = db.hashMap(this.id + "_dataset")
            .keySerializer(Serializer.STRING)
            .valueSerializer(Serializer.STRING)
            .createOrOpen();

//    ttl = db.treeMap(this.id + "_ttl")
//            .keySerializer(Serializer.LONG)
//            .valueSerializer(Serializer.STRING)
//            .createOrOpen();

  }

  @Override
  public void disconnect() {
    if(db != null)
      db.close();
  }

  @Override
  public void set(String id, JsonNode data, int ttlDays) throws Storage.StorageException {
    Record r = new Record(id, data, (long) ttlDays);
//    ttl.put(r.ttl, r.id);
    map.put(r.id, r.toString());
    db.commit();
  }

  @Override
  public JsonNode get(String id) throws Storage.StorageException {

    Record r = getRecord(id);

    if(r.ttl > 0 && Instant.ofEpochSecond(r.ttl).isBefore(Instant.now())) {
      delete(id);
      return null;
    }

    return r.content;
  }

  public Record getRecord(String id) throws Storage.StorageException {
    try {
      return parseRecord(map.get(id));
    }
    catch(IOException e) {
      throw new Storage.StorageException(e);
    }
  }

  @Override
  public List<JsonNode> list(ListQuery query) throws Storage.StorageException {
    /**
     * TODO: - add indeces in config, like per couchbase - on set() add maps for
     * each indexable key and its ref id - on list() match parameters with index
     * keys and create subsets to iterate over
     */
    throw new RuntimeException("Not Implemented");
    
//    List<JsonNode> results = new ArrayList();
//    query.getParams()
//    
//    for (Map.Entry<String, String> entry : map.getEntries()) {
//      Record r = parseRecord(entry.getValue());
//      results.add(r.content);
//    }
//    return results;
  }

  @Override
  public void delete(String id) throws Storage.StorageException {
    Record r = getRecord(id);
    map.remove(id);
//    ttl.remove(r.ttl);
  }

  protected Record parseRecord(String raw) throws IOException {
    return Storage.mapper.readValue(raw, Record.class);
  }

  private void setupIndexes() {
    
    if(!configuration.mapdb.indices.containsKey(this.id)) {
      return;
    }
    
    List<List<String>> indexDefinition = configuration.mapdb.indices.get(this.id);
    
  }
  
}
