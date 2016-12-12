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
package org.createnet.raptor.indexer.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
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
import org.createnet.raptor.indexer.AbstractIndexer;
import org.createnet.raptor.indexer.Indexer;
import org.createnet.raptor.indexer.Indexer.IndexerException;
import org.createnet.raptor.indexer.impl.es.ElasticSearchIndexAdmin;
import org.createnet.raptor.indexer.query.Query;
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
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ElasticSearchIndexer extends AbstractIndexer {

    final protected ObjectMapper mapper = Indexer.getObjectMapper();
    protected Client client;
    final protected Logger logger = LoggerFactory.getLogger(ElasticSearchIndexer.class);
    final private ElasticSearchIndexAdmin indexAdmin = new ElasticSearchIndexAdmin();

    /**
     *
     * @param file
     * @return
     */
    public static Map<String, String> loadIndicesFromFile(String file) {
        // Load indices.json to configuration
        Map<String, String> indices = new HashMap();

        ObjectMapper mapper = Indexer.getObjectMapper();
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
            if (response.status() == RestStatus.NOT_FOUND) {
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

    protected void connect() {

        String host = configuration.elasticsearch.transport.host;
        int port = configuration.elasticsearch.transport.port;

        switch (configuration.elasticsearch.type) {
            case "transport":

                try {
                    logger.debug("Connecting to ElasticSearch instance {}:{}", host, port);

                    Settings settings = Settings.builder()
                            .put(configuration.elasticsearch.clientConfig)
                            .build();

                    TransportAddress transportAddress = new InetSocketTransportAddress(InetAddress.getByName(host), port);

                    client = new PreBuiltTransportClient(settings)
                            .addTransportAddress(transportAddress);

                    // Wait for status update in case ES is booting
                    client.admin().cluster().prepareHealth().setWaitForYellowStatus().get();

                    this.indexAdmin.setClient(client);

                } catch (UnknownHostException uhe) {
                    throw new IndexerException(uhe);
                }

                break;
            default:
                throw new IndexerException("Unsupported connection type " + configuration.elasticsearch.type);
        }

    }

    /**
     *
     * @throws IndexerException
     */
    @Override
    public void open() throws IndexerException {

        int tries = 0, maxTries = 5, waitFor = 5000;

        while (true) {

            try {
                connect();
                return;
            } catch (Exception ex) {

                logger.warn("Connection to cluster failed: {}", ex.getMessage());

                if (tries >= maxTries) {
                    break;
                }

                try {
                    Thread.sleep(waitFor * tries);
                } catch (InterruptedException ex1) {
                    logger.warn("Cannot sleep current thread {}", ex1.getMessage());
                }

            }

            tries++;
        }

        throw new IndexerException("Connection failed");
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
            File file = new File(filepath);
            if (!file.exists()) {
                throw new IndexerException("Indices file not found " + configuration.elasticsearch.indices.source);
            }
            indices.putAll(ElasticSearchIndexer.loadIndicesFromFile(filepath));
        }

        indices.entrySet().forEach((el) -> {

            String indexName = el.getKey();
            String indexDefinition = el.getValue();

            try {

                if (indexDefinition.isEmpty()) {
                    throw new RuntimeException("Index `" + indexName + "` definition is empty! Check configurations and indices file");
                }

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

            } catch (Exception ex) {
                logger.error("Cannot complete setup phase: {}", ex.getMessage(), ex);
                throw new IndexerException(ex);
            }
        });

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
    public List<IndexRecord> search(Query query) throws SearchException {

        try {

            SearchRequestBuilder searchBuilder = client.prepareSearch(query.getIndex())
                    .setTypes(query.getType())
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setQuery((QueryBuilder) query.getNativeQuery());

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

            List<IndexRecord> list = new ArrayList();
            for (SearchHit hit : results) {
                list.add(
                        new IndexRecord(hit.getIndex(), hit.getType(), hit.getId(), hit.getSourceAsString())
                );
            }

            return list;
        } catch (Query.QueryException ex) {
            throw new SearchException(ex);
        } catch (Exception e) {
            throw new SearchException(e);
        }
    }

}
