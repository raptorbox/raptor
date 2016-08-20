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
package org.createnet.raptor.db;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import org.createnet.raptor.db.config.StorageConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
abstract public class AbstractStorage implements Storage {
    
  final protected Logger logger = LoggerFactory.getLogger(AbstractStorage.class);

  protected StorageConfiguration config;
  final protected Map<String, Connection> connections = new HashMap<>();

  static  public String generateId() {
    return UUID.randomUUID().toString();
  }

  public Map<String, Connection> getConnections() {
    return connections;
  }

  public void addConnection(Connection conn) {
    connections.put(conn.getId(), conn);
  }
  
  public void removeConnection(String id) {
    if(connections.containsKey(id)) {
      try {
        logger.warn("Disconnecting {}", id);
        connections.get(id).disconnect();
      }
      catch(Exception e) {
        logger.warn("Exception disconnecting {}", id, e);
      }
      connections.remove(id);
    }
  }

  @Override
  public Connection getConnection(String id) {
    return connections.get(id);
  }

  @Override
  public void disconnect() {
    Iterator<Map.Entry<String, Connection>> it = getConnections().entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, Connection> item = it.next();
      item.getValue().disconnect();
    }
  }

  @Override
  public void initialize(StorageConfiguration configuration) throws StorageException {
    this.config = configuration;
  }

  @Override
  public void destroy() {
    // does nothing by default
  }
  
  protected StorageConfiguration getConfiguration() {
    return config;
  }

  public void set(String connectionId, String id, JsonNode data, int ttl) throws StorageException {
    getConnection(connectionId).set(id, data, ttl);
  }

  public JsonNode get(String connectionId, String id) throws StorageException {
    return getConnection(connectionId).get(id);
  }

  public void delete(String connectionId, String id) throws StorageException {
    getConnection(connectionId).delete(id);
  }  
  
}
