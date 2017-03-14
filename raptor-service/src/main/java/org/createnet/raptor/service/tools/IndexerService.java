/*
 * Copyright 2017 FBK/CREATE-NET
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
package org.createnet.raptor.service.tools;

import org.createnet.raptor.service.AbstractRaptorService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.Stream;
import org.jvnet.hk2.annotations.Service;
import org.createnet.raptor.indexer.Indexer;
import org.createnet.raptor.indexer.IndexerConfiguration;
import org.createnet.raptor.indexer.IndexerProvider;
import org.createnet.raptor.indexer.query.Query;
import org.createnet.raptor.indexer.query.impl.es.DataQuery;
import org.createnet.raptor.indexer.query.impl.es.LastUpdateQuery;
import org.createnet.raptor.indexer.query.impl.es.ObjectListQuery;
import org.createnet.raptor.indexer.query.impl.es.ObjectQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles indexing and retrieval of data and object definitions
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
@Singleton
public class IndexerService extends AbstractRaptorService {

    private final Logger logger = LoggerFactory.getLogger(IndexerService.class);

    @Inject
    ConfigurationService configuration;

    @Inject
    CacheService cache;

    private IndexerConfiguration config = null;

    private Indexer indexer;

    public enum IndexNames {
        object, data, subscriptions
    }

    /**
     * Limit of records that can be fetched per request
     */
    private final int defaultLimit = 1000;

    /**
     * @return the limit of records per requests
     */
    public Integer getDefaultLimit() {
        if (config != null && config.recordFetchLimit != null) {
            return config.recordFetchLimit;
        }
        return defaultLimit;
    }

    @PostConstruct
    @Override
    public void initialize() {
        try {
            logger.debug("Initializing indexer");
            getIndexer();
        } catch (Indexer.IndexerException | ConfigurationException e) {
            throw new ServiceException(e);
        }
    }

    @PreDestroy
    @Override
    public void shutdown() {
        try {
            indexer = null;
            config = null;
            getIndexer().close();
        } catch (Indexer.IndexerException | ConfigurationException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Set the index informations based on well known index types
     *
     * @param query the Query to which apply the index context
     * @param name Stored enum name of the index configuration
     * @return the query object
     */
    public Query setQueryIndex(Query query, IndexNames name) {
        IndexerConfiguration.ElasticSearch.Indices.IndexDescriptor desc = getIndexDescriptor(name);
        query.setIndex(desc.index);
        query.setType(desc.type);
        return query;
    }

    /**
     *
     * Set the offset and limit of a query
     *
     * @param query the query to set
     * @param limit limit of record per request
     * @param offset offset to start from
     * @return the referenced query
     */
    public Query setCursor(Query query, Integer limit, Integer offset) {

        Integer queryLimit = getDefaultLimit();
        if (limit != null && (limit <= getDefaultLimit() && limit > 0)) {
            queryLimit = limit;
        }

        Integer queryOffset = 0;
        if (offset != null && offset > 0) {
            queryOffset = offset;
        }

        query.setOffset(queryOffset);
        query.setLimit(queryLimit);

        return query;
    }

    /**
     * Return (and initialize once) the indexer configured
     *
     * @return the indexer instance
     */
    public Indexer getIndexer() {

        if (indexer == null) {
            indexer = new IndexerProvider();
            config = configuration.getIndexer();
            indexer.initialize(config);
            indexer.open();
            indexer.setup(false);
        }

        return indexer;
    }

    /**
     * Return a well-known index configuration
     *
     * @param name well-known name of an index
     * @return the index configuration
     */
    protected IndexerConfiguration.ElasticSearch.Indices.IndexDescriptor getIndexDescriptor(IndexNames name) {
        return configuration.getIndexer().elasticsearch.indices.names.get(name.toString());
    }

    /**
     * Create a IndexRecord instance
     *
     * @param name name of well-known index
     * @return the index record
     */
    public Indexer.IndexRecord getIndexRecord(IndexNames name) {
        IndexerConfiguration.ElasticSearch.Indices.IndexDescriptor desc = getIndexDescriptor(name);
        return new Indexer.IndexRecord(desc.index, desc.type);
    }

    /**
     * List the objects created by an User
     *
     * @param userId id of an user
     * @param offset offset to start from
     * @param limit limit of record per call
     * @return list of objects
     */
    public List<ServiceObject> getObjectsByUser(String userId, Integer offset, Integer limit) {
        ObjectQuery q = new ObjectQuery();
        setCursor(q, limit, offset);
        q.setUserId(userId);
        return searchObject(q);
    }

    /**
     * List the objects created by an User
     *
     * @param userId id of an user
     * @return list of objects
     */
    public List<ServiceObject> getObjectsByUser(String userId) {
        return getObjectsByUser(userId, 0, getDefaultLimit());
    }

    /**
     * Load all objects specified by IDs list
     *
     * @param ids list of ids
     * @return list of ServiceObject instances
     */
    public List<ServiceObject> getObjects(List<String> ids) {

        List<ServiceObject> cached = ids.stream()
                .map((id) -> {
                    return cache.getObject(id);
                })
                .filter(o -> o != null)
                .collect(Collectors.toList());

        if (cached.size() == ids.size()) {
            return cached;
        }

        ObjectListQuery q = new ObjectListQuery();
        q.ids.addAll(ids);
        return searchObject(q);
    }

    /**
     * Load a single object
     *
     * @param id object id
     * @return ServiceObject instance
     */
    public ServiceObject getObject(String id) {

        List<ServiceObject> objs = getObjects(Arrays.asList(id));
        if (objs.isEmpty()) {
            return null;
        }

        ServiceObject obj = objs.get(0);
        return obj;
    }

    /**
     * Index a single object definition
     *
     * @param obj the ServiceObject instance
     * @param isNew if the object is newly created
     */
    public void saveObject(ServiceObject obj, boolean isNew) {

        Indexer.IndexRecord record = getIndexRecord(IndexNames.object);

        record.id = obj.id;
        record.body = obj.toJSON();

        // add to cache
        cache.setObject(obj);

        // force creation
        record.isNew(isNew);
        getIndexer().save(record);

    }

    /**
     * Remove an object from the index
     *
     * @param obj the object to remove
     */
    public void deleteObject(ServiceObject obj) {
        Indexer.IndexRecord record = getIndexRecord(IndexNames.object);
        record.id = obj.id;

        cache.clearObject(obj.id);

        getIndexer().delete(record);
        deleteData(obj.streams.values());
    }

    public List<ServiceObject> searchObject(Query query) {

        setQueryIndex(query, IndexNames.object);

        List<Indexer.IndexRecord> results = getIndexer().search(query);
        List<ServiceObject> list = new ArrayList();

        for (Indexer.IndexRecord result : results) {
            ServiceObject obj = ServiceObject.fromJSON(result.body);
            list.add(obj);
        }

        return list;
    }

    /**
     * Retrieve the last inserted record
     *
     * @param stream the stream reference
     * @return a RecordSet with the data
     */
    public RecordSet searchLastUpdate(Stream stream) {

        LastUpdateQuery lastUpdateQuery = new LastUpdateQuery(stream.getServiceObject().id, stream.name);
        setQueryIndex(lastUpdateQuery, IndexNames.data);

        lastUpdateQuery.setOffset(0);
        lastUpdateQuery.setLimit(1);
        lastUpdateQuery.setSort(new Query.SortBy(Query.Fields.timestamp, Query.Sort.DESC));

        List<Indexer.IndexRecord> results = getIndexer().search(lastUpdateQuery);

        if (results.isEmpty()) {
            return null;
        }

        return new RecordSet(stream, results.get(0).body);
    }

    /**
     * Create the stream record unique id
     *
     * @param rs a RecordSet with data
     * @return a unique id built upon RecordSet data
     * @throws Indexer.IndexerException on missing RecordSet data
     */
    protected String getDataId(RecordSet rs) {

        if (rs.objectId == null) {
            throw new Indexer.IndexerException("RecordSet.objectId cannot be null");
        }

        if (rs.streamId == null) {
            throw new Indexer.IndexerException("RecordSet.streamId cannot be null");
        }

        String time = "" + rs.getTimestamp().getTime();

        return String.join("-", new String[]{rs.objectId, rs.streamId, time});
    }

    /**
     * Save a single record of dat
     *
     * @param recordSet the object containing the data
     */
    public void saveData(RecordSet recordSet) {

        Indexer.IndexRecord record = getIndexRecord(IndexNames.data);
        record.isNew(true);

        record.id = getDataId(recordSet);
        record.body = recordSet.toJson();

        getIndexer().save(record);
    }

    /**
     * Save a list of records
     *
     * @param records A list of records to save
     */
    public void saveData(List<RecordSet> records) {

        if (records.size() == 1) {
            saveData(records.get(0));
            return;
        }

        List<Indexer.IndexOperation> ops = new ArrayList();
        records.stream().forEach((RecordSet record) -> {

            Indexer.IndexRecord r = getIndexRecord(IndexNames.data);
            r.id = getDataId(record);
            r.body = record.toJson();

            Indexer.IndexOperation op = new Indexer.IndexOperation(Indexer.IndexOperation.Type.CREATE, r);
            ops.add(op);
        });

        getIndexer().batch(ops);
    }

    /**
     * Save a list of objects
     *
     * @param ids list of objects to save
     */
    public void saveObjects(List<ServiceObject> ids) {
        saveObjects(ids, null);
    }

    /**
     * Save a list of objects
     *
     * @param ids list of objects to save
     * @param isNew define if the objects are new or is an update operation.
     * null means upsert (insert or update)
     */
    public void saveObjects(List<ServiceObject> ids, Boolean isNew) {

        if (ids.size() == 1) {
            saveObject(ids.get(0), isNew);
            return;
        }

        List<Indexer.IndexOperation> ops = new ArrayList();
        ids.stream().forEachOrdered((obj) -> {
            Indexer.IndexRecord record = getIndexRecord(IndexNames.object);
            record.id = obj.id;
            if (isNew != null) {
                record.isNew(isNew);
            }

            record.body = obj.toJSON();

            // add cache
            cache.setObject(obj);
            Indexer.IndexOperation.Type opType;
            if (isNew == null) {
                opType = Indexer.IndexOperation.Type.UPSERT;
            } else {
                opType = isNew ? Indexer.IndexOperation.Type.CREATE : Indexer.IndexOperation.Type.UPDATE;
            }
            Indexer.IndexOperation op = new Indexer.IndexOperation(opType, record);
            ops.add(op);
        });

        getIndexer().batch(ops);
    }

    /**
     * Return the data for a stream
     *
     * @param stream the stream to get the data from
     * @param limit number of records to return
     * @param offset offset to start from
     * @return a list of RecordSet objects
     */
    public List<RecordSet> getStreamData(Stream stream, Integer limit, Integer offset) {

        DataQuery query = new DataQuery();
        setQueryIndex(query, IndexNames.data);

        query
                .setMatch(Query.Fields.streamId, stream.name);

        List<Indexer.IndexRecord> records = getIndexer().search(query);
        List<RecordSet> results = new ArrayList();

        records.stream().forEachOrdered((record) -> {
            results.add(new RecordSet(stream, record.body));
        });

        return results;
    }

    /**
     * Delete all the data for a stream
     *
     * @param stream the stream for which to delete the data
     *
     * @TODO https://github.com/raptorbox/raptor/issues/33
     */
    public void deleteData(Stream stream) {

        int offset = 0,
                limit = getDefaultLimit(),
                lastCount = -1;

        List<RecordSet> results = getStreamData(stream, limit, offset);
        while (!results.isEmpty()) {

            int len = results.size();
            logger.debug("Removing indexer data for {}", stream.name);

            List<Indexer.IndexOperation> deletes = new ArrayList();
            results.stream().forEachOrdered((recordSet) -> {
                Indexer.IndexRecord record = getIndexRecord(IndexNames.data);
                record.id = stream.getServiceObject().id + "-" + stream.name + "-" + recordSet.getTimestamp().getTime();
                deletes.add(new Indexer.IndexOperation(Indexer.IndexOperation.Type.DELETE, record));
            });

            getIndexer().batch(deletes);

            offset += limit;
            results = getStreamData(stream, limit, offset);
            if (lastCount > -1) {
                if (results.size() == len && len == lastCount && offset > len) {
                    logger.warn("Loop detected removing data for {}, quitting", stream.name);
                    break;
                }
            }
            lastCount = results.size();
        }
    }

    /**
     * Delete all the data from the provided streams
     *
     * @param streams list of streams
     */
    public void deleteData(Collection<Stream> streams) {
        streams.forEach((stream) -> {
            deleteData(stream);
        });
    }

    /**
     * Search for data
     *
     * @param stream the reference stream
     * @param query the data query
     * @param limit number of records
     * @param offset offset to start from
     * @return the list of RecordSet with data
     */
    public ResultSet searchData(Stream stream, DataQuery query, Integer limit, Integer offset) {
        setCursor(query, limit, offset);
        return searchData(stream, query);
    }

    /**
     * Search for data
     *
     * @param stream the reference stream
     * @param query the data query
     * @return the list of RecordSet with data
     */
    public ResultSet searchData(Stream stream, DataQuery query) {

        setQueryIndex(query, IndexNames.data);
        List<Indexer.IndexRecord> records = getIndexer().search(query);

        ResultSet resultset = new ResultSet(stream);
        records.forEach((record) -> {
            resultset.add(new RecordSet(stream, record.body));
        });

        return resultset;
    }

    /**
     * Fetch data for a stream
     *
     * @param stream the stream to fetch data from
     * @return a ResultSet with records
     */
    public ResultSet fetchData(Stream stream) {
        return fetchData(stream, getDefaultLimit(), 0);
    }

    /**
     * Fetch data for a stream
     *
     * @param stream the stream to fetch data from
     * @param limit number of records to return
     * @param offset offset to start from
     * @return a ResultSet with records
     */
    public ResultSet fetchData(Stream stream, Integer limit, Integer offset) {

        // query for all the data
        DataQuery query = new DataQuery();
        setQueryIndex(query, IndexNames.data);
        setCursor(query, limit, offset);

        query.setSort(new Query.SortBy(Query.Fields.timestamp, Query.Sort.DESC));
        query.timeRange(Instant.EPOCH);

        ResultSet data = searchData(stream, query);
        return data;
    }

    /**
     * Fetch the last inserted record
     *
     * @param stream the stream reference
     * @return the RecordSet with data
     */
    public RecordSet fetchLastUpdate(Stream stream) {
        ResultSet data = fetchData(stream, 1, 0);
        return data.size() > 0 ? data.get(0) : null;
    }

}
