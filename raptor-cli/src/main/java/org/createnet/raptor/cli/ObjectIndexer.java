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
package org.createnet.raptor.cli;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.db.query.BaseQuery;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.createnet.raptor.service.tools.IndexerService;
import org.createnet.raptor.service.tools.StorageService;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.search.Indexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ObjectIndexer {

  final private Logger logger = LoggerFactory.getLogger(ObjectIndexer.class);
  
  final private StorageService storage;
  final private IndexerService indexer;

  final private int batchSize = 500;

  public class ObjectIndexerException extends RuntimeException {

    public ObjectIndexerException(String message) {
      super(message);
    }

    public ObjectIndexerException(Throwable cause) {
      super(cause);
    }

  }

  public ObjectIndexer(StorageService storage, IndexerService indexer) {
    this.storage = storage;
    this.indexer = indexer;
  }

  public void sync() {
        
    int offset = 0;
    int limit = batchSize;

    logger.debug("Sync started, batch size at {}", batchSize);
    
    while (true) {
      
      List<ServiceObject> list = getObjects(offset, limit);
      
      logger.debug("Retrieved {} records", list.size());
      
      if (list.isEmpty()) {
        logger.debug("Records list is empty, completed sync");
        break;
      }

      runBatch(list);
      
      offset += limit;
    }

  }

  protected void runBatch(List<ServiceObject> list) {

    try {
      
      logger.debug("Start index batch");
      List<Indexer.IndexOperation> batchList = new ArrayList();

      int i = 0;
      for (ServiceObject obj : list) {

        Indexer.IndexRecord indexRecord = indexer.getIndexRecord(IndexerService.IndexNames.object);

        indexRecord.id = obj.id;
        indexRecord.body = obj.toJSON();

        Indexer.IndexOperation op = new Indexer.IndexOperation(Indexer.IndexOperation.Type.UPSERT, indexRecord);
        batchList.add(op);

        i++;

        if (i == batchSize) {
          i = 0;
          logger.debug("Send batch operation of {} elements", batchSize);
          indexer.getIndexer().batch(batchList);
          batchList.clear();
        }

      }

      if (!batchList.isEmpty()) {
        logger.debug("Send batch operation of {} elements", batchList.size());
        indexer.getIndexer().batch(batchList);
        batchList.clear();
      }

    } catch (RaptorComponent.ParserException | ConfigurationException | Indexer.IndexerException e) {
      logger.debug("Exception while indexing", e);
      throw new ObjectIndexerException(e);
    }

  }

  protected List<ServiceObject> getObjects(int offset, int limit) {
    try {

      BaseQuery query = new BaseQuery();

      query.offset = offset;
      query.limit = limit;
      
      logger.debug("Loading {} objects from offset {}", limit, offset);
      
      List<JsonNode> objList = storage.getObjectConnection().list(query);

      List<ServiceObject> list = new ArrayList();

      for (JsonNode rawobj : objList) {
        list.add(ServiceObject.fromJSON(rawobj));
      }

      return list;
    } catch (ConfigurationException | Storage.StorageException ex) {
      logger.debug("Exception while loading objects", ex);
      throw new ObjectIndexerException(ex);
    }
  }

}
