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
import java.util.List;
import javax.inject.Inject;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.http.exception.ConfigurationException;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.exception.RecordsetException;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.Stream;
import org.jvnet.hk2.annotations.Service;
import org.createnet.search.raptor.search.Indexer;
import org.createnet.search.raptor.search.IndexerConfiguration;
import org.createnet.search.raptor.search.IndexerProvider;
import org.createnet.search.raptor.search.query.AbstractQuery;
import org.createnet.search.raptor.search.query.Query;
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
  
  protected enum IndexNames {
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
    return configuration.getIndexer().elasticsearch.indices.names.get(name.name());
  }
  
  protected Indexer.IndexRecord getIndexRecord(IndexNames name) throws ConfigurationException {
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
    record.body = obj.toJSON();
    
    // force creation
    record.isNew(isNew);
    
    getIndexer().save(record);
  }
  
  public void deleteObject(String id) throws ConfigurationException, Indexer.IndexerException{
    
    Indexer.IndexRecord record = getIndexRecord(IndexNames.object);
    record.id = id;
    
    getIndexer().delete(record);
  }

  public List<String> searchObject(ObjectQuery query) throws Indexer.SearchException, IOException, ConfigurationException, Authentication.AuthenticationException, RaptorComponent.ParserException {
    
    setQueryIndex(query, IndexNames.object);
    query.setUserId(auth.getUser().getUserId());
    
    List<String> results = indexer.search(query);
    List<String> list = new ArrayList();
    
    for(String result : results) {
      list.add(ServiceObject.fromJSON(result).id);
    }
    
    return list;
  }

  public RecordSet searchLastUpdate(Stream stream) throws ConfigurationException, Indexer.SearchException, RecordsetException {
    
    LastUpdateQuery lastUpdateQuery = new LastUpdateQuery(stream.getServiceObject().id, stream.name);
    setQueryIndex(lastUpdateQuery, IndexNames.data);
    
    lastUpdateQuery.setOffset(0);
    lastUpdateQuery.setLimit(1);
    lastUpdateQuery.setSort(new Query.SortBy("lastUpdate", Query.Sort.DESC));
    
    List<String> results = indexer.search(lastUpdateQuery);
    
    if(results.isEmpty()) {
      return null; 
    }
    
    return new RecordSet(stream, results.get(0));
  }
  
  public void indexData(Stream stream, RecordSet recordSet) throws ConfigurationException, IOException, Indexer.IndexerException  {
    
    Indexer.IndexRecord record = getIndexRecord(IndexNames.data);
    record.id = stream.getServiceObject().id + "-" + stream.name + "-" + recordSet.getLastUpdate().getTime();
    record.isNew(true);
    
    ObjectNode data = (ObjectNode) recordSet.toJsonNode();
    
    data.put("stream", stream.name);
    data.put("objectId", stream.getServiceObject().getId());
    
    record.body = data.toString();
    
    getIndexer().save(record);
  }
  
}
