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

  public String type;
  public Couchbase couchbase;
  
  public class Couchbase {

    public List<String> nodes;
    public String username;
    public String password;
    public Map<String, String> buckets;
    public BucketDefaults bucketDefaults;
    
    public class BucketDefaults {
      public String password;
      public int quota;
      public int replica;
      public boolean indexReplica;
      public boolean enableFlush;
      
    }
  }

}
