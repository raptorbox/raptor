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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.createnet.raptor.db.config.StorageConfiguration;
import org.createnet.raptor.db.query.ListQuery;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public interface Storage {

  static public enum ConnectionId {
    objects, data, actuations
  }

  static public ObjectMapper mapper = new ObjectMapper();

  public static ConnectionId getConnectionId(String name) {
    return ConnectionId.valueOf(name);
  }
  
  public static class StorageException extends RuntimeException {

    public StorageException(Throwable e) {
      super(e);
    }

    public StorageException(String e) {
      super(e);
    }
  }

  public interface Connection {

    public String getId();

    public void initialize(StorageConfiguration configuration);

    public void setup(boolean forceSetup) throws StorageException;

    public void connect() throws StorageException;

    public void disconnect();

    public void destroy();

    public void set(String id, JsonNode data, int ttlDays) throws StorageException;

    public JsonNode get(String id) throws StorageException;

    public List<JsonNode> list(ListQuery query) throws StorageException;

    public void delete(String id) throws StorageException;

  }

  public void initialize(StorageConfiguration configuration) throws StorageException;

  public void setup(boolean forceSetup) throws StorageException;

  public void connect() throws StorageException;

  public void disconnect();

  public void destroy();

  public Connection getConnection(String id);

}
