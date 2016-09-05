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
package org.createnet.raptor.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.db.query.BaseQuery;
import org.createnet.raptor.http.service.IndexerService;
import org.createnet.raptor.http.service.StorageService;
import org.createnet.raptor.search.raptor.search.Indexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Parameters(separators = "=", commandDescription = "Index record from storage")
public class IndexCommand implements Command {

  protected static String TYPE_DEFINITION = "definition";
  protected static String TYPE_DATA = "data";

  final private Logger logger = LoggerFactory.getLogger(IndexCommand.class);

  @Inject
  StorageService storage;

  @Inject
  IndexerService indexer;

  @Parameter(names = {"-b", "--batch-size"}, description = "Number of records to index per operation")
  public int batchSize = 500;

  @Parameter(names = {"-t", "--type"}, description = "Type of data to index, may be `data` or `definition`")
  public List<String> dataType = new ArrayList();

  public class SyncObject {

    public boolean completed = false;
    final public List<Indexer.IndexRecord> items = new ArrayList();

    private void setItems(List<Indexer.IndexRecord> objects) {
      items.addAll(objects);
    }
  }

  public class ObjectIndexerException extends Exception {

    public ObjectIndexerException(String message) {
      super(message);
    }

    public ObjectIndexerException(Throwable cause) {
      super(cause);
    }

  }

  public void sync() throws ObjectIndexerException, ConfigurationException {

    int offset = 0;
    int limit = batchSize;
    
    int completed = 0;
    int types = 0;
    
    logger.debug("Syncing definition, batch size at {}", batchSize);

    Map<String, SyncObject> recordsMap = new HashMap();

    if (dataType.contains(TYPE_DATA)) {
      recordsMap.put(TYPE_DATA, new SyncObject());
      // add type
    }

    if (dataType.contains(TYPE_DEFINITION)) {
      recordsMap.put(TYPE_DEFINITION, new SyncObject());
    }
    
    types = recordsMap.size();
    
    while (true) {

      if (recordsMap.containsKey(TYPE_DATA)) {
        recordsMap.get(TYPE_DATA).setItems(getData(offset, limit));
      }

      if (recordsMap.containsKey(TYPE_DEFINITION)) {
        recordsMap.get(TYPE_DEFINITION).setItems(getObjects(offset, limit));
      }

      for (Map.Entry<String, SyncObject> entry : recordsMap.entrySet()) {

        String key = entry.getKey();
        SyncObject value = entry.getValue();

        if (value.completed) {
          continue;
        }

        List<Indexer.IndexRecord> list = value.items;

        logger.debug("Retrieved {} records", list.size());

        if (list.isEmpty()) {
          logger.debug("Records list is empty, completed sync on {}", key);
          value.completed = true;
          completed++;
          break;
        }

        runBatch(list);
        value.items.clear();
      }

      if(types == completed) {
        logger.debug("Completed");
        break;
      }
      
      offset += limit;
    }

  }

  protected void runBatch(List<Indexer.IndexRecord> list) throws ObjectIndexerException, ConfigurationException {

    try {

      logger.debug("Start index batch");
      List<Indexer.IndexOperation> batchList = new ArrayList();

      int i = 0;
      for (Indexer.IndexRecord indexRecord : list) {

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

    } catch (Indexer.IndexerException e) {
      logger.debug("Exception while indexing", e);
      throw new ObjectIndexerException(e);
    }

  }

  protected List<Indexer.IndexRecord> getObjects(int offset, int limit) throws ObjectIndexerException {
    try {

      BaseQuery query = new BaseQuery();

      query.offset = offset;
      query.limit = limit;

      logger.debug("Loading {} objects from offset {}", limit, offset);

      List<JsonNode> objList = storage.getObjectConnection().list(query);
      List<Indexer.IndexRecord> list = new ArrayList();

      for (JsonNode rawobj : objList) {

        Indexer.IndexRecord indexRecord = indexer.getIndexRecord(IndexerService.IndexNames.object);

        indexRecord.id = rawobj.get("id").asText();
        indexRecord.body = rawobj.toString();

        list.add(indexRecord);
      }

      return list;
    } catch (ConfigurationException | Storage.StorageException ex) {
      logger.debug("Exception while loading objects", ex);
      throw new ObjectIndexerException(ex);
    }
  }

  private List<Indexer.IndexRecord> getData(int offset, int limit) throws ObjectIndexerException {
    try {

      BaseQuery query = new BaseQuery();
      query.offset = offset;
      query.limit = limit;

      logger.debug("Loading {} data records from offset {}", limit, offset);

      List<JsonNode> objList = storage.getDataConnection().list(query);
      List<Indexer.IndexRecord> list = new ArrayList();

      for (JsonNode rawobj : objList) {

        Indexer.IndexRecord indexRecord = indexer.getIndexRecord(IndexerService.IndexNames.data);

        
        indexRecord.id = rawobj.get("objectId").asText() + "-" + rawobj.get("streamId").asText() + "-" + rawobj.get("lastUpdate").asText();
        indexRecord.body = rawobj.toString();
        indexRecord.isNew(true);
        
        list.add(indexRecord);
      }

      return list;
    } catch (ConfigurationException | Storage.StorageException ex) {
      logger.debug("Exception while loading data records", ex);
      throw new ObjectIndexerException(ex);
    }
  }
  
  @Override
  public String getName() {
    return "index";
  }

  @Override
  public void run() throws CommandException {
    try {
      sync();
    } catch (ObjectIndexerException | ConfigurationException ex) {
      throw new CommandException(ex);
    }
  }
  
}
