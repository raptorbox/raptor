/*
 * Copyright 2016 Luca Capra <lcapra@create-net.org>.
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
package org.createnet.raptor.http.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.exception.RecordsetException;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.models.objects.serializer.ServiceObjectView;
import org.jvnet.hk2.annotations.Service;
import org.createnet.raptor.search.raptor.search.Indexer;
import org.createnet.raptor.search.raptor.search.IndexerConfiguration;
import org.createnet.raptor.search.raptor.search.IndexerProvider;
import org.createnet.raptor.search.raptor.search.query.Query;
import org.createnet.raptor.search.raptor.search.query.impl.es.DataQuery;
import org.createnet.raptor.search.raptor.search.query.impl.es.LastUpdateQuery;
import org.createnet.raptor.search.raptor.search.query.impl.es.ObjectListQuery;
import org.createnet.raptor.search.raptor.search.query.impl.es.ObjectQuery;
import org.createnet.raptor.search.raptor.search.query.impl.es.TreeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class IndexerService implements RaptorService {

    private final Logger logger = LoggerFactory.getLogger(IndexerService.class);

    @Inject
    ConfigurationService configuration;

    /**
     * Limit of records that can be fetched per request
     */
    private final int defaultRecordLimit = 1000;

    private Indexer indexer;

    public enum IndexNames {
        object, data, subscriptions, groups
    }

    @PostConstruct
    @Override
    public void initialize() throws ServiceException {
        try {
            getIndexer();
        } catch (Indexer.IndexerException | ConfigurationException e) {
            throw new ServiceException(e);
        }
    }

    @PreDestroy
    @Override
    public void shutdown() throws ServiceException {
        try {
            getIndexer().close();
            indexer = null;
        } catch (Indexer.IndexerException | ConfigurationException e) {
            throw new ServiceException(e);
        }
    }

    public Indexer getIndexer() throws Indexer.IndexerException, ConfigurationException {

        if (indexer == null) {
            indexer = new IndexerProvider();
            indexer.initialize(configuration.getIndexer());
            indexer.open();
            indexer.setup(false);
        }

        return indexer;
    }

    protected IndexerConfiguration.ElasticSearch.Indices.IndexDescriptor getIndexDescriptor(IndexNames name) throws ConfigurationException {
        return configuration.getIndexer().elasticsearch.indices.names.get(name.toString());
    }

    public Indexer.IndexRecord getIndexRecord(IndexNames name) throws ConfigurationException {
        IndexerConfiguration.ElasticSearch.Indices.IndexDescriptor desc = getIndexDescriptor(name);
        return new Indexer.IndexRecord(desc.index, desc.type);
    }

    private void setQueryIndex(Query query, IndexNames name) throws ConfigurationException {
        IndexerConfiguration.ElasticSearch.Indices.IndexDescriptor desc = getIndexDescriptor(name);
        query.setIndex(desc.index);
        query.setType(desc.type);
    }

    @Deprecated
    public List<ServiceObject> getObjects(String userId) throws ConfigurationException, Authentication.AuthenticationException, RaptorComponent.ParserException, Indexer.IndexerException {
        ObjectQuery q = new ObjectQuery();
        q.setUserId(userId);
        return searchObject(q);
    }

    public List<ServiceObject> getObjects(List<String> ids) throws ConfigurationException, Authentication.AuthenticationException, RaptorComponent.ParserException, Indexer.IndexerException {
        ObjectListQuery q = new ObjectListQuery();
        q.ids.addAll(ids);
        return searchObject(q);
    }

    public void indexObject(ServiceObject obj, boolean isNew) throws ConfigurationException, Indexer.IndexerException, RaptorComponent.ParserException, Authentication.AuthenticationException {
        Indexer.IndexRecord record = getIndexRecord(IndexNames.object);
        record.id = obj.id;
        record.body = obj.toJSON(ServiceObjectView.Internal);

        // force creation
        record.isNew(isNew);
        getIndexer().save(record);

        if (!lookup(obj.id)) {
            throw new Indexer.IndexerException("Index timeout while processing " + obj.id);
        }

    }

    /**
     * @TODO Remove on upgrade to ES 5.x
     */    
    @Deprecated
    protected boolean lookup(String objectId) {
        int max = 5, curr = max, wait = 500; //ms
        while (curr > 0) {
            try {
                List<ServiceObject> objs = getObjects(Arrays.asList(objectId));
                if (!objs.isEmpty()) {
                    logger.warn("Object {} avail in index after {}ms", objectId, (max - curr) * wait);
                    return true;
                }
                Thread.sleep(wait);
            } catch (Exception ex) {
            } finally {
                curr--;
            }
        }
        return false;
    }

    public void deleteObject(ServiceObject obj) throws ConfigurationException, Indexer.IndexerException, IOException, RecordsetException {
        Indexer.IndexRecord record = getIndexRecord(IndexNames.object);
        record.id = obj.id;
        getIndexer().delete(record);
        deleteData(obj.streams.values());
    }

    public List<ServiceObject> searchObject(Query query) throws Indexer.SearchException, ConfigurationException, Authentication.AuthenticationException, RaptorComponent.ParserException, Indexer.IndexerException {

        setQueryIndex(query, IndexNames.object);

        List<String> results = getIndexer().search(query);
        List<ServiceObject> list = new ArrayList();

        for (String result : results) {
            list.add(ServiceObject.fromJSON(result));
        }

        return list;
    }

    public RecordSet searchLastUpdate(Stream stream) throws ConfigurationException, Indexer.SearchException, RecordsetException, Indexer.IndexerException {

        LastUpdateQuery lastUpdateQuery = new LastUpdateQuery(stream.getServiceObject().id, stream.name);
        setQueryIndex(lastUpdateQuery, IndexNames.data);

        lastUpdateQuery.setOffset(0);
        lastUpdateQuery.setLimit(1);
        lastUpdateQuery.setSort(new Query.SortBy("lastUpdate", Query.Sort.DESC));

        List<String> results = getIndexer().search(lastUpdateQuery);

        if (results.isEmpty()) {
            return null;
        }

        return new RecordSet(stream, results.get(0));
    }

    public void indexData(Stream stream, RecordSet recordSet) throws ConfigurationException, IOException, Indexer.IndexerException, Authentication.AuthenticationException {

        Indexer.IndexRecord record = getIndexRecord(IndexNames.data);
        record.id = stream.getServiceObject().id + "-" + stream.name + "-" + recordSet.getLastUpdate().getTime();
        record.isNew(true);

        ObjectNode data = (ObjectNode) recordSet.toJsonNode();

        data.put("streamId", stream.name);
        data.put("objectId", stream.getServiceObject().getId());
        data.put("userId", stream.getServiceObject().getUserId());

        record.body = data.toString();

        getIndexer().save(record);
    }
    
    public void saveObjects(List<ServiceObject> ids) throws Indexer.IndexerException, ConfigurationException, RaptorComponent.ParserException {

        List<Indexer.IndexOperation> ops = new ArrayList();
        for (ServiceObject obj : ids) {
            Indexer.IndexRecord record = getIndexRecord(IndexNames.object);
            record.id = obj.id;
            record.isNew(true);
            record.body = obj.toJSON(ServiceObjectView.Internal);
            Indexer.IndexOperation op = new Indexer.IndexOperation(Indexer.IndexOperation.Type.UPSERT, record);
        }

        getIndexer().batch(ops);
        lookup(ids.get(0).id);
    }
    
    public List<RecordSet> getStreamData(Stream stream) throws ConfigurationException, Indexer.IndexerException, RecordsetException {

        DataQuery query = new DataQuery();
        setQueryIndex(query, IndexNames.data);

        query.match = true;
        query.matchfield = "streamId";
        query.matchstring = stream.name;

        List<String> rawResults = getIndexer().search(query);
        List<RecordSet> results = new ArrayList();

        for (String result : rawResults) {
            RecordSet recordSet = new RecordSet(stream, result);
            results.add(recordSet);
        }

        return results;
    }

    public void deleteData(Stream stream) throws ConfigurationException, IOException, Indexer.IndexerException, RecordsetException {

        List<Indexer.IndexOperation> deletes = new ArrayList();
        List<RecordSet> results = getStreamData(stream);

        for (RecordSet recordSet : results) {

            Indexer.IndexRecord record = getIndexRecord(IndexNames.data);
            record.id = stream.getServiceObject().id + "-" + stream.name + "-" + recordSet.getLastUpdate().getTime();

            Indexer.IndexOperation op = new Indexer.IndexOperation(Indexer.IndexOperation.Type.DELETE, record);
            deletes.add(op);
        }

        getIndexer().batch(deletes);

    }

    public ResultSet searchData(Stream stream, DataQuery query) throws Indexer.SearchException, RecordsetException, Indexer.IndexerException, ConfigurationException {

        setQueryIndex(query, IndexNames.data);
        List<String> res = getIndexer().search(query);

        ResultSet resultset = new ResultSet(stream);
        for (String raw : res) {
            resultset.add(new RecordSet(stream, raw));
        }

        return resultset;
    }

    public void deleteData(Collection<Stream> changedStreams) throws ConfigurationException, IOException, Indexer.IndexerException, RecordsetException {
        for (Stream changedStream : changedStreams) {
            deleteData(changedStream);
        }
    }

    public ResultSet fetchData(Stream stream) throws ConfigurationException, IOException, Indexer.IndexerException, RecordsetException {
        return fetchData(stream, 0);
    }

    public ResultSet fetchData(Stream stream, long limit) throws ConfigurationException, IOException, Indexer.IndexerException, RecordsetException {

        // query for all the data
        DataQuery query = new DataQuery();
        setQueryIndex(query, IndexNames.data);

        query.setLimit(defaultRecordLimit);
        query.setSort(new Query.SortBy("lastUpdate", Query.Sort.DESC));
        query.timeRange(Instant.EPOCH);

        ResultSet data = searchData(stream, query);
        return data;
    }

    public RecordSet fetchLastUpdate(Stream stream) throws ConfigurationException, IOException, Indexer.IndexerException, RecordsetException {
        ResultSet data = fetchData(stream, 1);
        return data.size() > 0 ? data.get(0) : null;
    }

    public List<String> getChildrenList(ServiceObject parentObject) throws Indexer.SearchException, Indexer.IndexerException, ConfigurationException, Authentication.AuthenticationException, RaptorComponent.ParserException {
        TreeQuery query = new TreeQuery();
        query.queryType = TreeQuery.TreeQueryType.Children;
        query.parentId = parentObject.parentId;
        query.id = parentObject.id;
        setQueryIndex(query, IndexNames.groups);
        return getIndexer().search(query)
                .stream()
                .map((String s) -> {
                    TreeQuery.TreeRecordBody record = ServiceObject.getMapper().convertValue(s, TreeQuery.TreeRecordBody.class);
                    return record.id;
                })
                .collect(Collectors.toList());
    }

    public List<ServiceObject> getChildren(ServiceObject parentObject) throws Indexer.SearchException, Indexer.IndexerException, ConfigurationException, Authentication.AuthenticationException, RaptorComponent.ParserException {
        
        List<String> ids = getChildrenList(parentObject);
        
        List<ServiceObject> list = new ArrayList();
        if(!ids.isEmpty())
            list = getObjects(ids);
        
        return list;
    }

    public void setChildrenList(ServiceObject obj, List<String> list) throws ConfigurationException, RaptorComponent.ParserException, Indexer.IndexerException {

        logger.debug("Storing children list for {}: {}", obj.id, String.join(",", list));

        List<Indexer.IndexOperation> ops = new ArrayList();
        for (String childId : list) {
            Indexer.IndexRecord record = getIndexRecord(IndexNames.groups);
            record.id = obj.getId();
            record.isNew(true);
            TreeQuery.TreeRecordBody treeRecord = new TreeQuery.TreeRecordBody();
            treeRecord.id = childId;
            treeRecord.parentId = obj.id;
            treeRecord.path = obj.parentId == null ? obj.id : obj.parentId + "/" + obj.id;
            try {
                record.body = ServiceObject.getMapper().writeValueAsString(treeRecord);
            } catch (JsonProcessingException ex) {
                throw new RaptorComponent.ParserException(ex);
            }
            Indexer.IndexOperation op = new Indexer.IndexOperation(Indexer.IndexOperation.Type.UPSERT, record);
        }

        getIndexer().batch(ops);
    }

}
