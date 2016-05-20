/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import org.createnet.raptor.db.config.StorageConfiguration;
import org.createnet.raptor.db.couchbase.CouchbaseStorage;
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
  
  public void removeConnection(String bucketId) {
    if(connections.containsKey(bucketId)) {
      try {
        connections.get(bucketId).disconnect();
      }
      catch(Exception e) {
        logger.warn("Exception disconnecting " + bucketId, e);
      }
      connections.remove(bucketId);
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

  public void set(String connectionId, String id, String data, int ttl) throws StorageException {
    getConnection(connectionId).set(id, data, ttl);
  }

  public String get(String connectionId, String id) throws StorageException {
    return getConnection(connectionId).get(id);
  }

  public void delete(String connectionId, String id) throws StorageException {
    getConnection(connectionId).delete(id);
  }  
  
}
