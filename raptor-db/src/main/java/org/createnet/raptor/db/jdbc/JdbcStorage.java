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
package org.createnet.raptor.db.jdbc;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import org.createnet.raptor.db.AbstractStorage;
import org.createnet.raptor.db.config.StorageConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class JdbcStorage extends AbstractStorage {

  final protected Logger logger = LoggerFactory.getLogger(JdbcStorage.class);

  protected java.sql.Connection connection = null;

  @Override
  public void connect() throws StorageException {
    
    if(connection != null) {
      return;
    }
    
    try {
      connection = DriverManager.getConnection(getConfiguration().jdbc.connection);
    } catch (SQLException e) {
      connection = null;
      throw new StorageException(e);
    }
  }

  @Override
  public void destroy() {
    try {

      Statement statement = connection.createStatement();
      statement.setQueryTimeout(30);  // set timeout to 30 sec.

      for (Map.Entry<String, StorageConfiguration.Jdbc.Table> entry : getConfiguration().jdbc.tables.entrySet()) {
        String objName = entry.getKey();
        StorageConfiguration.Jdbc.Table table = entry.getValue();
        logger.debug("Removing table {} ({})", table.name, objName);
        statement.executeUpdate(String.format("drop table if exists %s", table.name));
      }

    } catch (SQLException ex) {
      logger.error("Error on destroy: {}", ex.getMessage());
    }

  }

  @Override
  public void disconnect() {
    super.disconnect();
    try {
      if (connection != null) {
        connection.close();
        connection = null;
      }
    } catch (SQLException e) {
      logger.error("Disconnect failed: {}", e.getMessage());
    }
  }

  @Override
  public void setup(boolean forceSetup) throws StorageException {

    if (forceSetup) {
      logger.debug("Dropping previous tables");
      destroy();
    }

   
    Statement statement = null;
    try {
      statement = connection.createStatement();      
      statement.setQueryTimeout(10);
    } catch (SQLException ex) {
      throw new StorageException(ex);
    }
    
    for (Map.Entry<String, StorageConfiguration.Jdbc.Table> entry : getConfiguration().jdbc.tables.entrySet()) {
  
      StringBuilder sb = new StringBuilder("create table %s if not exists ( \nid text,\n content text");

      
      String objName = entry.getKey();
      StorageConfiguration.Jdbc.Table table = entry.getValue();
      
      logger.debug("Creating table {} for store {}", table.name, objName);
      
      if(table.fields.size() > 0) {
        for (Map.Entry<String, String> field : table.fields.entrySet()) {
          sb.append(",\n ");
          sb.append(field.getKey());
          sb.append(" ");
          sb.append(field.getValue());
        }
      }
      
      sb.append(")");
      
      try {

        String sql = String.format(sb.toString(), table.name);
        logger.debug("EXecuting sql: {}", sql);
        statement.executeUpdate(sql);

      } catch (SQLException ex) {
        logger.debug("Failed to create table {}: {}", table.name, ex.getMessage());
        throw new StorageException(ex);
      }
    }

    logger.debug("Setup completed");
  }

}
