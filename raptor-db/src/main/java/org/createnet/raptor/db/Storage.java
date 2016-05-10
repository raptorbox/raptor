/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.db;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public interface Storage {

  public class StorageException extends Exception {
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
    public void connect();
    
    public void set(String id, String data, int ttlSeconds);
    public String get(String id);
    public void delete(String id);

  }

  public void initialize(Map<String, Object> configuration);  
  public void setup(boolean forceSetup);  
  public void connect();
  public void disconnect();
  
  public Connection getConnection(String id);
  
}
