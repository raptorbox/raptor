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
package org.createnet.search.raptor.search.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.createnet.search.raptor.search.AbstractIndexer;
import org.createnet.search.raptor.search.Indexer;
import org.createnet.search.raptor.search.Indexer.IndexerException;
import org.createnet.search.raptor.search.IndexerConfiguration;
import org.createnet.search.raptor.search.impl.es.ElasticSearchIndexAdmin;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ElasticSearchIndexer extends AbstractIndexer {

  final protected ObjectMapper mapper = new ObjectMapper();
  protected Client client;
  final protected Logger logger = LoggerFactory.getLogger(ElasticSearchIndexer.class);
  final private ElasticSearchIndexAdmin indexAdmin = new ElasticSearchIndexAdmin();

  public ElasticSearchIndexer(IndexerConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   *
   * @param file
   * @return
   * @throws IOException
   */
  public static Map<String, String> loadIndicesFromFile(String file) throws IOException {
    // Load indices.json to configuration
    Map<String, String> indices = new HashMap();

    ObjectMapper mapper = new ObjectMapper();
    JsonNode json = mapper.readTree(Files.readAllBytes(Paths.get(file)));

    Iterator<String> it = json.fieldNames();
    while (it.hasNext()) {
      String indexName = it.next();
      indices.put(indexName, json.get(indexName).toString());
    }

    return indices;
  }

  /**
   *
   * @param record
   * @throws IndexerException
   */
  @Override
  public void save(IndexRecord record) throws IndexerException {
    if (record.isNew()) {
      create(record);
    } else {
      update(record);
//      upsert(record);          
    }

  }

  protected void upsert(IndexRecord record) throws IndexerException {

    try {

      IndexRequest indexRequest = new IndexRequest(record.index, record.type, record.id)
              .source(record.body);

      UpdateRequest updateRequest = new UpdateRequest(record.index, record.type, record.id)
              .doc(record.body)
              .timeout(getTimeout())
              .upsert(indexRequest);

      client.update(updateRequest).get();

    } catch (InterruptedException | ExecutionException e) {
      throw new IndexerException(e);
    }

  }

  /**
   *
   * @param record
   * @throws IndexerException
   */
  protected void update(IndexRecord record) throws IndexerException {
    try {

      logger.debug("Update index record to {}.{}", record.index, record.type);

      UpdateRequest updateRequest = new UpdateRequest()
              .source(record.body.getBytes());
      UpdateResponse response = client.update(updateRequest)
              .actionGet(getTimeout());
      if (!response.isCreated()) {
        throw new Exception("Record update failed");
      }

    } catch (Exception ex) {
      logger.warn("Record update failed on {}.{}.{}", record.index, record.type, record.id);
      throw new IndexerException(ex);
    }

  }

  /**
   *
   * @param record
   * @throws IndexerException
   */
  protected void create(IndexRecord record) throws IndexerException {

    try {

      logger.debug("Create index record to {}.{}", record.index, record.type);
      IndexResponse response = client.prepareIndex(record.index, record.type, record.id)
              .setSource(record.body)
              .setTimeout(getTimeout())
              .get();

      if (!response.isCreated()) {
        logger.warn("Record creation failed on {}.{}", record.index, record.type);
        throw new Exception("Record creation failed");
      }

    } catch (Exception e) {
      throw new IndexerException(e);
    }
  }

  /**
   *
   * @param record
   * @throws IndexerException
   */
  @Override
  public void delete(IndexRecord record) throws IndexerException {
    logger.debug("Delete index record {}.{}.{}", record.index, record.type, record.id);
    try {
      DeleteResponse response = client.prepareDelete(record.index, record.type, record.id).get();
      if (!response.isFound()) {
        throw new IndexerException("Record not found");
      }
    } catch (Exception e) {
      logger.warn("Record deletion failed on {}.{}.{}", record.index, record.type, record.id);
      throw new IndexerException(e);
    }
  }

  /**
   *
   * @param list
   * @throws IndexerException
   */
  @Override
  public void batch(List<Indexer.IndexOperation> list) throws IndexerException {
    try {

      logger.debug("Executing batch on {} items", list.size());

      BulkProcessor bulkProcessor = BulkProcessor.builder(
        client,  
        new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId,
                                   BulkRequest request) {
              logger.debug("Starting bulk operation");
            } 

            @Override
            public void afterBulk(long executionId,
                                  BulkRequest request,
                                  BulkResponse response) {
              logger.debug("Bulk operation completed");
            } 

            @Override
            public void afterBulk(long executionId,
                                  BulkRequest request,
                                  Throwable failure) {
              logger.error("Bulk failed", failure);
            } 
        })
        .setBulkActions(5000) 
        .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.MB)) 
        .setFlushInterval(TimeValue.timeValueSeconds(1)) 
        .setConcurrentRequests(2) 
        .setBackoffPolicy(
            BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
        .build();      

      Iterator<IndexOperation> it = list.iterator();
      while (it.hasNext()) {

        IndexOperation operation = it.next();

        IndexRecord record = operation.record;

        switch (operation.type) {

          case CREATE:
            bulkProcessor.add(
                    client.prepareIndex(record.index, record.type, record.id)
                    .setSource(record.body)
                    .request()
            );
            break;

          case UPDATE:
            bulkProcessor.add(
                    client.prepareUpdate(record.index, record.type, record.id)
                    .setDoc(record.body)
                    .request()
            );

            break;

          case SAVE:
          case UPSERT:

            bulkProcessor.add(
                    client.prepareUpdate(record.index, record.type, record.id)
                    .setDoc(record.body)
                    .setUpsert(
                            client.prepareIndex(record.index, record.type, record.id)
                            .setSource(record.body).request()
                    ).request()
            );

            break;
          case DELETE:
            bulkProcessor.add(
                    client.prepareDelete(record.index, record.type, record.id).request()
            );
            break;
        }
        
      }
      
      boolean res = bulkProcessor.awaitClose(10, TimeUnit.MINUTES);
      logger.debug("Completed batch: result {}", res);

    } catch (Exception e) {
      logger.warn("Batch operation failed");
      throw new IndexerException(e);
    }

  }

  /**
   *
   * @throws IndexerException
   */
  @Override
  public void open() throws IndexerException {

    String host = configuration.elasticsearch.transport.host;
    int port = configuration.elasticsearch.transport.port;

    try {

      logger.debug("Connecting to ElasticSearch instance {}:{}", host, port);

      Settings settings = Settings.settingsBuilder()
              .put(configuration.elasticsearch.clientConfig)
              .build();

      TransportAddress transportAddress = new InetSocketTransportAddress(InetAddress.getByName(host), port);
      client = TransportClient.builder()
              .settings(settings)
              .build()
              .addTransportAddress(transportAddress);

      this.indexAdmin.setClient(client);

    } catch (UnknownHostException uhe) {
      throw new IndexerException(uhe);
    }
  }

  /**
   *
   * @param forceSetup
   * @throws IndexerException
   */
  @Override
  public void setup(boolean forceSetup) throws IndexerException {

    logger.debug("Setup client");

    Map<String, String> indices = configuration.elasticsearch.indices;

    for (Map.Entry<String, String> el : indices.entrySet()) {

      String indexName = el.getKey();
      String indexDefinition = el.getValue();

      try {

        boolean indexExists = indexAdmin.exists(indexName);

        if (indexExists) {
          if (forceSetup) {
            logger.debug("Force setup, dropping index {}", indexName);
            indexAdmin.delete(indexName);
            indexExists = false;
          }
        }

        if (!indexExists) {
          indexAdmin.create(indexName, indexDefinition);
        }

      } catch (InterruptedException | ExecutionException | ElasticSearchIndexAdmin.IndexAdminException ex) {
        logger.error("Cannot complete setup phase", ex);
        throw new IndexerException(ex);
      }
    }

  }

  /**
   *
   * @throws IndexerException
   */
  @Override
  public void close() throws IndexerException {

    logger.debug("Closing client");

    try {
      if (client != null) {
        client.close();
      }
    } catch (Exception e) {
      throw new IndexerException(e);
    }

  }


  public static void main(String[] argv) throws IndexerException, IOException {

    // enable ES logging
    ESLoggerFactory.getRootLogger().setLevel("DEBUG");

    final Logger mainLogger = LoggerFactory.getLogger("main");

    IndexerConfiguration configuration = new IndexerConfiguration();
    
    configuration.type = "elasticsearch";
    
    configuration.elasticsearch.type = "transport";
    
    configuration.elasticsearch.transport.host = "127.0.0.1";
    configuration.elasticsearch.transport.port = 9300;
    
    configuration.elasticsearch.indicesSource = "/etc/raptor/indices.json";
    
    
//    String indexFile = "indices.json";
    String dataFile = "data.json";
    ClassLoader classLoader = ElasticSearchIndexer.class.getClassLoader();

//    String filepath = classLoader.getResource(indexFile).getPath();
    String filepath = configuration.elasticsearch.indicesSource;
    Map<String, String> indices = ElasticSearchIndexer.loadIndicesFromFile(filepath);

    configuration.elasticsearch.indices.putAll(indices);
    
    Map<String, String> clientConfig = new HashMap();
    clientConfig.put("cluster.name", "raptor");

    configuration.elasticsearch.clientConfig.putAll(clientConfig);
            
    Indexer indexer = new ElasticSearchIndexer(configuration);

    indexer.open();
    indexer.setup(true);

    String dataFilepath = classLoader.getResource(dataFile).getPath();
    String data = new String(Files.readAllBytes(Paths.get(dataFilepath)));

//    for (int i = 0; i < 100; i++) {
//      mainLogger.debug("Push {}", i);
//      try {
//        UUID uuid = UUID.randomUUID();
//        IndexRecord record = new IndexRecord("soupdates", "update", uuid.toString(), data);
//        ops.add(new IndexOperation(IndexOperation.Type.SAVE, record));
//        indexer.save(record);
//      } catch (IndexerException ex) {
//        mainLogger.error("Error indexing", ex);
//      } finally {
//        mainLogger.debug("Completed");
//      }
//    }

    List<Indexer.IndexOperation> ops = new ArrayList();
    for (int i = 0; i < 10000; i++) {
      mainLogger.debug("Push {}", i);
      UUID uuid = UUID.randomUUID();
      IndexRecord record = new IndexRecord("soupdates", "update", uuid.toString(), data);
      ops.add(new IndexOperation(IndexOperation.Type.CREATE, record));
    }

    indexer.batch(ops);

//    indexer.close();

  }  
  
}
