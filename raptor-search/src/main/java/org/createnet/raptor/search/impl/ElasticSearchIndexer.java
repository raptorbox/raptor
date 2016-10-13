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
package org.createnet.raptor.search.impl;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.createnet.raptor.search.raptor.search.AbstractIndexer;
import org.createnet.raptor.search.raptor.search.Indexer;
import org.createnet.raptor.search.raptor.search.Indexer.IndexerException;
import org.createnet.raptor.search.raptor.search.impl.es.ElasticSearchIndexAdmin;
import org.createnet.raptor.search.raptor.search.query.Query;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
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

    /**
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static Map<String, String> loadIndicesFromFile(String file) {
        // Load indices.json to configuration
        Map<String, String> indices = new HashMap();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json;
        try {
            json = mapper.readTree(Files.readAllBytes(Paths.get(file)));
        } catch (IOException ex) {
            throw new IndexerException(ex);
        }

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
    public void save(IndexRecord record) {
        if (record.isNew()) {
            create(record);
        } else {
            update(record);
//      upsert(record);          
        }

    }

    protected void upsert(IndexRecord record) {

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

            UpdateResponse response = client.prepareUpdate(record.index, record.type, record.id)
                    .setDoc(record.body.getBytes())
                    .get(getTimeout());

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

        } catch (Exception e) {
            logger.warn("Record creation failed on {}.{}.{}", record.index, record.type, record.id);
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
                    .setBulkActions(list.size())
                    .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.MB))
                    .setFlushInterval(TimeValue.timeValueSeconds(1))
                    .setConcurrentRequests(2)
                    .setBackoffPolicy(
                            BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3)
                    )
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

        logger.debug("Setup client, force {}", forceSetup);

        Map<String, String> indices = configuration.elasticsearch.indices.definitions;

        if (indices.isEmpty()) {
            String filepath = configuration.elasticsearch.indices.source;
            indices.putAll(ElasticSearchIndexer.loadIndicesFromFile(filepath));
        }

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

            } catch (ElasticSearchIndexAdmin.IndexAdminException ex) {
                logger.error("Cannot complete setup phase: {}", ex.getMessage(), ex);
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

    @Override
    public List<String> search(Query query) throws SearchException {

        try {

            SearchRequestBuilder searchBuilder = client.prepareSearch(query.getIndex())
                    .setTypes(query.getType())
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setQuery(query.format());

            if (query.getLimit() != null && query.getLimit() > 0) {
                searchBuilder.setSize(query.getLimit());
            }

            if (query.getOffset() != null) {
                searchBuilder.setFrom(query.getOffset());
            }

            if (query.getSort() != null) {
                searchBuilder.addSort(query.getSort().field, query.getSort().sort == Query.Sort.ASC ? SortOrder.ASC : SortOrder.DESC);
            }

            logger.debug("Search query: {}", searchBuilder.toString());

            SearchResponse response = searchBuilder.execute().actionGet();

            logger.debug("Found {} records in {}", response.getHits().getTotalHits(), response.getTook().toString());

            SearchHit[] results = response.getHits().getHits();

            List<String> list = new ArrayList();
            for (SearchHit hit : results) {
                list.add(hit.getSourceAsString());
            }

            return list;
        } catch (Query.QueryException ex) {
            throw new SearchException(ex);
        }
    }

//  public static void main(String[] argv)  {
//
//    // enable ES logging
//    ESLoggerFactory.getRootLogger().setLevel("DEBUG");
//
//    final Logger mainLogger = LoggerFactory.getLogger("main");
//
//    IndexerConfiguration configuration = new IndexerConfiguration();
//
//    configuration.type = "elasticsearch";
//
//    configuration.elasticsearch.type = "transport";
//
//    configuration.elasticsearch.transport.host = "raptor.local";
//    configuration.elasticsearch.transport.port = 9300;
//
//    configuration.elasticsearch.indices.source = "/etc/raptor/indices.json";
//
////    String indexFile = "indices.json";
//    String dataFile = "data.json";
//    ClassLoader classLoader = ElasticSearchIndexer.class.getClassLoader();
//
////    String filepath = classLoader.getResource(indexFile).getPath();
//    String filepath = configuration.elasticsearch.indices.source;
//    Map<String, String> indices = ElasticSearchIndexer.loadIndicesFromFile(filepath);
//
//    configuration.elasticsearch.indices.definitions.putAll(indices);
//
//    Map<String, String> clientConfig = new HashMap();
//    clientConfig.put("cluster.name", "raptor");
//
//    configuration.elasticsearch.clientConfig.putAll(clientConfig);
//
//    Indexer indexer = new ElasticSearchIndexer();
//    indexer.initialize(configuration);
//
//    indexer.open();
//    indexer.setup(true);
//
//    String dataFilepath = classLoader.getResource(dataFile).getPath();
//    String data = new String(Files.readAllBytes(Paths.get(dataFilepath)));
//
////    for (int i = 0; i < 100; i++) {
////      mainLogger.debug("Push {}", i);
////      try {
////        UUID uuid = UUID.randomUUID();
////        IndexRecord record = new IndexRecord("soupdates", "update", uuid.toString(), data);
////        ops.add(new IndexOperation(IndexOperation.Type.SAVE, record));
////        indexer.save(record);
////      } catch (IndexerException ex) {
////        mainLogger.error("Error indexing", ex);
////      } finally {
////        mainLogger.debug("Completed");
////      }
////    }
////    List<Indexer.IndexOperation> ops = new ArrayList();
////    for (int i = 0; i < 10000; i++) {
////      mainLogger.debug("Push {}", i);
////      UUID uuid = UUID.randomUUID();
////      IndexRecord record = new IndexRecord("soupdates", "update", uuid.toString(), data);
////      ops.add(new IndexOperation(IndexOperation.Type.CREATE, record));
////    }
////    indexer.batch(ops);
////    indexer.close();
//  }
}
