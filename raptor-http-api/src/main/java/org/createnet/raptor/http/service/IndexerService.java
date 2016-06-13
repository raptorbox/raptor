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

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import org.createnet.search.raptor.search.Indexer;
import org.createnet.search.raptor.search.IndexerConfiguration;
import org.createnet.search.raptor.search.IndexerProvider;
import org.createnet.search.raptor.search.query.AbstractQuery;
import org.createnet.search.raptor.search.query.Query;
import org.createnet.search.raptor.search.query.impl.es.DataQuery;
import org.createnet.search.raptor.search.query.impl.es.LastUpdateQuery;
import org.createnet.search.raptor.search.query.impl.es.ObjectQuery;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class IndexerService {

  @Inject
  ConfigurationService configuration;

  @Inject
  AuthService auth;

  private Indexer indexer;
 
  public enum IndexNames {
    object, data, subscriptions
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

  private void setQueryIndex(AbstractQuery query, IndexNames name) throws ConfigurationException {
    IndexerConfiguration.ElasticSearch.Indices.IndexDescriptor desc = getIndexDescriptor(name);
    query.setIndex(desc.index);
    query.setType(desc.type);
  }
  
  public void indexObject(ServiceObject obj, boolean isNew) throws ConfigurationException, Indexer.IndexerException, RaptorComponent.ParserException {
    
    Indexer.IndexRecord record = getIndexRecord(IndexNames.object);
    record.id = obj.id;
    record.body = obj.toJSON(ServiceObjectView.Internal);
    
    // force creation
    record.isNew(isNew);
    
    getIndexer().save(record);
  }
  
  public void deleteObject(ServiceObject obj) throws ConfigurationException, Indexer.IndexerException, IOException, RecordsetException{
    
    Indexer.IndexRecord record = getIndexRecord(IndexNames.object);
    record.id = obj.id;
    
    getIndexer().delete(record);
    
    deleteData(obj.streams.values());
    
  }

  public List<String> searchObject(ObjectQuery query) throws Indexer.SearchException, IOException, ConfigurationException, Authentication.AuthenticationException, RaptorComponent.ParserException, Indexer.IndexerException {
    
    setQueryIndex(query, IndexNames.object);
    query.setUserId(auth.getUser().getUserId());
    
    List<String> results = getIndexer().search(query);
    List<String> list = new ArrayList();
    
    for(String result : results) {
      list.add(ServiceObject.fromJSON(result).id);
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
    
    if(results.isEmpty()) {
      return null; 
    }
    
    return new RecordSet(stream, results.get(0));
  }
  
  public void indexData(Stream stream, RecordSet recordSet) throws ConfigurationException, IOException, Indexer.IndexerException, Authentication.AuthenticationException  {
    
    Indexer.IndexRecord record = getIndexRecord(IndexNames.data);
    record.id = stream.getServiceObject().id + "-" + stream.name + "-" + recordSet.getLastUpdate().getTime();
    record.isNew(true);
    
    ObjectNode data = (ObjectNode) recordSet.toJsonNode();
    
    data.put("streamId", stream.name);
    data.put("objectId", stream.getServiceObject().getId());
    data.put("userId", auth.getUser().getUserId());
    
    record.body = data.toString();
    
    getIndexer().save(record);
  }
  
  public void deleteData(Stream stream) throws ConfigurationException, IOException, Indexer.IndexerException, RecordsetException  {
    
    DataQuery query = new DataQuery();
    setQueryIndex(query, IndexNames.data);
    
    query.match = true;
    query.matchfield = "streamId";
    query.matchstring = stream.name;
    
    List<Indexer.IndexOperation> deletes = new ArrayList();
    List<String> results = getIndexer().search(query);
    
    for (String result : results) {
      
      RecordSet recordSet = new RecordSet(stream, result);
      
      Indexer.IndexRecord record = getIndexRecord(IndexNames.data);
      record.id = stream.getServiceObject().id + "-" + stream.name + "-" + recordSet.getLastUpdate().getTime();
      
      Indexer.IndexOperation op = new Indexer.IndexOperation(Indexer.IndexOperation.Type.DELETE, record);
      deletes.add(op);
    }
    getIndexer().batch(deletes);
    
  }
  
  public List<ResultSet> searchData(Stream stream, DataQuery query) throws Indexer.SearchException, RecordsetException, Indexer.IndexerException, ConfigurationException {
    List<ResultSet> results = new ArrayList();
    
    List<String> res = getIndexer().search(query);
    
    for (String raw : res) {
      results.add(new ResultSet(stream, raw));
    }
    
    return results;
  }

  public void deleteData(Collection<Stream> changedStreams) throws ConfigurationException, IOException, Indexer.IndexerException, RecordsetException {
    for (Stream changedStream : changedStreams) {
      deleteData(changedStream);
    }
  }

  
}
