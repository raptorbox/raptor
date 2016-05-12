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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.createnet.raptor.http.exception.ConfigurationException;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.jvnet.hk2.annotations.Service;
import org.createnet.search.raptor.search.Indexer;
import org.createnet.search.raptor.search.IndexerConfiguration;
import org.createnet.search.raptor.search.IndexerProvider;
import org.createnet.search.raptor.search.query.impl.ObjectQuery;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class IndexerService {

  @Inject
  ConfigurationService configuration;

  private Indexer indexer;
  
  protected enum IndexNames {
    object, data, subscriptions
  }
  
  protected Indexer getIndexer() throws Indexer.IndexerException, ConfigurationException {

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

  public List<ServiceObject> searchObject(ObjectQuery query) throws Indexer.SearchException, IOException, ConfigurationException {
    
    IndexerConfiguration.ElasticSearch.Indices.IndexDescriptor desc = getIndexDescriptor(IndexNames.object);        
    query.setIndex(desc.index);
    query.setType(desc.type);

    
    List<String> results = indexer.search(query);
    List<ServiceObject> list = new ArrayList();
    
    for(String result : results) {
      list.add(ServiceObject.fromJSON(result));
    }
    
    return list;
  }

  
}
