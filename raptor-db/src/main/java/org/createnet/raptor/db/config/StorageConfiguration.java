package org.createnet.raptor.db.config;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class StorageConfiguration {

  public StorageConfiguration() {
  }
  
  public String type;
  public Couchbase couchbase = new Couchbase();
  
  static public class Couchbase {

    public Couchbase() {
    }
    
    public List<String> nodes;
    public String username;
    public String password;
    public Map<String, String> buckets;
    public Map<String, List<List<String>>> bucketsIndex;
    public BucketDefaults bucketDefaults;
    
    static public class BucketDefaults {

      public BucketDefaults() {
      }
      
      public String password;
      public int quota;
      public int replica;
      public boolean indexReplica;
      public boolean enableFlush;
      
    }
  }

}
