/*
 * Copyright 2016 CREATE-NET
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
package org.createnet.raptor.db.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketType;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.ClusterManager;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import java.util.Iterator;
import java.util.Map;
import org.createnet.raptor.db.AbstractStorage;
import org.createnet.raptor.db.config.StorageConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class CouchbaseStorage extends AbstractStorage {

  final Logger logger = LoggerFactory.getLogger(CouchbaseStorage.class);

  protected Cluster cluster;

  protected Cluster connectCluster() {

    if (cluster == null) {
      // Connect to localhost
      logger.debug("Connecting to couchbase cluster");
      cluster = CouchbaseCluster.create(getConfiguration().couchbase.nodes);
    }

    return cluster;
  }

  protected void connectBuckets() throws StorageException {

    Map<String, String> buckets = getConfiguration().couchbase.buckets;

    ClusterManager clusterManager = getClusterManager();

    Iterator<Map.Entry<String, String>> it = buckets.entrySet().iterator();
    while (it.hasNext()) {

      Map.Entry<String, String> item = it.next();

      String bucketId = item.getKey();
      String bucketName = item.getValue();

      if (getConnection(bucketName) != null) {
        continue;
      }

      boolean exists = clusterManager.hasBucket(bucketName);

      if (!exists) {
        logger.debug("Skipped connection to non-existan bucket {}", bucketName);
        continue;
      }

      logger.debug("Connecting bucket {}", bucketName);

      Bucket bucket = cluster.openBucket(bucketName, getConfiguration().couchbase.bucketDefaults.password);
      Connection conn = new CouchbaseConnection(bucketId, bucket);

      conn.initialize(getConfiguration());

      addConnection(conn);
    }
  }

  @Override
  public void connect() throws StorageException {
    connectCluster();
    connectBuckets();
  }

  @Override
  public void destroy() {
    
    Map<String, String> buckets = getConfiguration().couchbase.buckets;
    
    ClusterManager clusterManager = getClusterManager();
    for (Map.Entry<String, String> el : buckets.entrySet()) {
      logger.debug("Removing bucket {}", el.getValue());
      clusterManager.removeBucket(el.getValue());
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
  public void setup(boolean forceSetup) throws StorageException {

    logger.debug("Setup database");

    connectCluster();

    Map<String, String> buckets = getConfiguration().couchbase.buckets;

    ClusterManager clusterManager = getClusterManager();
    for (Map.Entry<String, String> el : buckets.entrySet()) {

      String bucketName = el.getValue();

      boolean exists = clusterManager.hasBucket(bucketName);

      if (exists && forceSetup) {
        logger.debug("Drop bucket {}", bucketName);
        clusterManager.removeBucket(bucketName);
        exists = false;
      }

      if (!exists) {

        StorageConfiguration.Couchbase.BucketDefaults bucketDef = getConfiguration().couchbase.bucketDefaults;

        logger.debug("Creating bucket {}", bucketName);
        BucketSettings bucketSettings = new DefaultBucketSettings.Builder()
                .type(BucketType.COUCHBASE)
                .name(bucketName)
                .password(bucketDef.password)
                .quota(bucketDef.quota) // megabytes
                .replicas(bucketDef.replica)
                .indexReplicas(bucketDef.indexReplica)
                .enableFlush(bucketDef.enableFlush)
                .build();

        clusterManager.insertBucket(bucketSettings);
      }

    }

    connectBuckets();

    for (Map.Entry<String, Connection> entry : getConnections().entrySet()) {
      String bucketId = entry.getKey();
      Connection conn = entry.getValue();
      conn.connect();
      conn.setup(forceSetup);
    }

    logger.debug("Setup completed");
  }

  private ClusterManager getClusterManager() {
    String adminUsername = getConfiguration().couchbase.username;
    String adminPassword = getConfiguration().couchbase.password;

    ClusterManager clusterManager = cluster.clusterManager(adminUsername, adminPassword);
    return clusterManager;
  }

}
