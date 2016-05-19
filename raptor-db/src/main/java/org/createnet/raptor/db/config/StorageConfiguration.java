package org.createnet.raptor.db.config;

import java.util.ArrayList;
import java.util.HashMap;
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
    
    public List<String> nodes = new ArrayList();
    public String username;
    public String password;
    public Map<String, String> buckets = new HashMap();
    public Map<String, List<List<String>>> bucketsIndex = new HashMap();
    public BucketDefaults bucketDefaults = new BucketDefaults();
    
    static public class BucketDefaults {

      public BucketDefaults() {
      }
      
      public String password = "";
      public int quota = 120;
      public int replica = 0;
      public boolean indexReplica = false;
      public boolean enableFlush = false;
      
    }
  }

}
