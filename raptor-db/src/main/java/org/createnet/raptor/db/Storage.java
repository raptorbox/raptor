/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.db;

import java.util.List;
import org.createnet.raptor.db.config.StorageConfiguration;
import org.createnet.raptor.db.query.ListQuery;



/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public interface Storage {

  public static class StorageException extends Exception {
    public StorageException(Throwable e) {
      super(e);
    }
    public StorageException(String e) {
      super(e);
    }
  }
  
  public interface Connection {

    public String getId();
    
    public void disconnect();
    public void connect() throws StorageException;
    public void setup(boolean forceSetup) throws StorageException;
    public void initialize(StorageConfiguration configuration);
    
    public void set(String id, String data, int ttlDays) throws StorageException;
    public String get(String id) throws StorageException;
    public List<String> list(ListQuery query) throws StorageException;
    public void delete(String id) throws StorageException;

  }

  public void initialize(StorageConfiguration configuration) throws StorageException;  
  public void setup(boolean forceSetup) throws StorageException;
  public void connect() throws StorageException;
  public void disconnect();
  
  public Connection getConnection(String id);
  
}
