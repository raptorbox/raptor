/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.db.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketType;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.ClusterManager;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.document.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.createnet.raptor.db.AbstractStorage;
import org.createnet.raptor.db.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class CouchbaseStorage extends AbstractStorage {

  final Logger logger = LoggerFactory.getLogger(CouchbaseStorage.class);

  final String clusterDefaultPassword = "";
  final String bucketDefaultPassword = "";

  final int bucketDefaultQuota = 240;
  final int bucketDefaultReplica = 0;
  final boolean bucketDefaultIndexReplica = false;
  final boolean bucketDefaultEnableFlush = false;

  protected Cluster cluster;

  protected Cluster connectCluster() {

    if (cluster == null) {
      // Connect to localhost
      logger.debug("Connecting to couchbase cluster");
      cluster = CouchbaseCluster.create((List<String>) getConfiguration().get("nodes"));
    }

    return cluster;
  }

  @Override
  public void connect() {

    connectCluster();

    Map<String, String> buckets = (Map<String, String>) getConfiguration().get("buckets");

    Iterator<Map.Entry<String, String>> it = buckets.entrySet().iterator();
    while (it.hasNext()) {

      Map.Entry<String, String> item = it.next();

      if (getConnection(item.getKey()) != null) {
        continue;
      }

      logger.debug("Connecting bucket {}", item.getValue());

      Bucket bucket = cluster.openBucket(item.getValue(), clusterDefaultPassword);
      Connection conn = new CouchbaseConnection(item.getKey(), bucket);

      conn.connect();

      addConnection(conn);
    }

  }

  @Override
  public void disconnect() {

    logger.debug("Disconnecting from couchbase");

    super.disconnect();
    // Disconnect and clear all allocated resources
    cluster.disconnect();
  }

  @Override
  public void setup(boolean forceSetup) {

    logger.debug("Setup database");

    connectCluster();

    Map<String, String> buckets = (Map<String, String>) getConfiguration().get("buckets");

    String adminUsername = (String) getConfiguration().get("username");
    String adminPassword = (String) getConfiguration().get("password");

    ClusterManager clusterManager = cluster.clusterManager(adminUsername, adminPassword);
    for (Map.Entry<String, String> el : buckets.entrySet()) {
      String bucketName = el.getValue();

      boolean exists = clusterManager.hasBucket(bucketName);

      if (exists && forceSetup) {
        logger.debug("Drop bucket {}", bucketName);
        clusterManager.removeBucket(bucketName);
        exists = false;
      }

      if (!exists) {

        logger.debug("Creating bucket {}", bucketName);
        BucketSettings bucketSettings = new DefaultBucketSettings.Builder()
                .type(BucketType.COUCHBASE)
                .name(bucketName)
                .password(bucketDefaultPassword)
                .quota(bucketDefaultQuota) // megabytes
                .replicas(bucketDefaultReplica)
                .indexReplicas(bucketDefaultIndexReplica)
                .enableFlush(bucketDefaultEnableFlush)
                .build();

        clusterManager.insertBucket(bucketSettings);
      }
    }

    logger.debug("Setup completed");
  }

  public static void main(String[] argv) {
    
    final Logger mainLogger = LoggerFactory.getLogger("mainLogger");
    
    Map<String, Object> config = new HashMap<>();

    config.put("username", "Administrator");
    config.put("password", "password");

    List<String> nodes = new ArrayList();
    nodes.add("servioticy.local");
    config.put("nodes", nodes);

    Map<String, String> buckets = new HashMap<>();
    buckets.put("so", "serviceobjects");
    buckets.put("data", "soupdates");
    buckets.put("subscriptions", "sosubscriptions");

    config.put("buckets", buckets);

    Storage storage = new CouchbaseStorage();

    storage.initialize(config);

    storage.setup(false);
    storage.connect();

    for(int i = 0; i < 500; i++) {
      
      mainLogger.debug("Inserting {}", i);
      
      String id = AbstractStorage.generateId();
      JsonObject obj = JsonObject
              .empty()
              .put("id", id)
              .put("name", "something")
              .put("customFields", JsonObject.jo());
      ;
      
      storage.getConnection("so").set(id, obj.toString(), 0);      
    }

    storage.disconnect();

  }

}
