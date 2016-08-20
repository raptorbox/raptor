/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.db.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.query.Index;
import com.couchbase.client.java.query.N1qlParams;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.consistency.ScanConsistency;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.createnet.raptor.db.AbstractConnection;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.db.query.ListQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class CouchbaseConnection extends AbstractConnection {

  private final Logger logger = LoggerFactory.getLogger(CouchbaseConnection.class);

  final Bucket bucket;

  public CouchbaseConnection(String id, Bucket bucket) {
    this.id = id;
    this.bucket = bucket;
  }

  @Override
  public void disconnect() {
    bucket.close();
  }

  @Override
  public void destroy() {
  }

  @Override
  public void connect() {
  }

  @Override
  public void set(String id, String data, int ttlDays) {

    JsonObject obj = JsonObject.fromJson(data);

    int ttl = ttlDays;
    if (ttlDays > 0) {
      Calendar c = Calendar.getInstance();
      c.setTime(new Date());
      c.add(Calendar.DATE, ttlDays);
      ttl = (int) (c.getTime().getTime() / 1000);
    }

    JsonDocument doc = JsonDocument.create(id, ttl, obj);
    bucket.upsert(doc);
  }

  public void set(String id, String data) {
    set(id, data, 0);
  }

  @Override
  public String get(String id) {
    JsonDocument doc = bucket.get(id);
    if (doc == null) {
      return null;
    }
    return doc.content().toString();
  }

  @Override
  public void delete(String id) {
    try {
      bucket.remove(id);
    }
    catch(DocumentDoesNotExistException ex) {
      logger.debug("Cannot remove doc, doesn't exists {}", id);
    }
      
  }

  @Override
  public List<String> list(ListQuery query) throws Storage.StorageException {

    String selectQuery = "SELECT * FROM `" + bucket.name() + "`";

    if (!query.getParams().isEmpty()) {
      selectQuery += " WHERE ";
      for (ListQuery.QueryParam param : query.getParams()) {
        selectQuery += " `" + param.key + "` " + param.operation + " \"" + param.value + "\" AND";
      }
      selectQuery = selectQuery.substring(0, selectQuery.length() - 3);
    }

    if (query.getSort() != null) {
      ListQuery.SortBy sort = query.getSort();
      selectQuery += " ORDER BY " + sort.getField() + " " + sort.getSort();
    }

    if (query.getLimit() > 0) {
      selectQuery += " LIMIT " + query.getLimit();
    }

    if (query.getOffset() > 0) {
      selectQuery += " OFFSET " + query.getOffset();
    }

    logger.debug("Performing N1QL query: {}", selectQuery);

    N1qlParams ryow = N1qlParams.build().consistency(ScanConsistency.REQUEST_PLUS);
    
    ListQuery.QueryOptions queryOptions = query.getQueryOptions();
    N1qlQueryResult results = null;
    int i = queryOptions.retries;
    while (i > 0) {
      try {
        results = bucket.query(N1qlQuery.simple(selectQuery, ryow), queryOptions.timeout, queryOptions.timeoutUnit);
        break;
      } catch (RuntimeException ex) {
        logger.error("Runtime exception on couchbase.list: {}", ex.getMessage());
        results = null;
      } finally {
        i--;
      }
    }

    if (results == null) {
      logger.warn("Couchbase query failed");
      throw new Storage.StorageException("List query cannot be completed");
    }

    List<String> list = new ArrayList();
    if (!results.errors().isEmpty()) {
      String errors = "";
      for (JsonObject err : results.errors()) {
        errors += "\n - " + err.toString();
      }
      throw new Storage.StorageException("N1QL query exception: " + errors);
    }

    Iterator<N1qlQueryRow> it = results.allRows().iterator();
    while (it.hasNext()) {
      N1qlQueryRow row = it.next();
      list.add(row.value().get(bucket.name()).toString());
    }

    return list;
  }

  protected String getIndexName(List<String> fieldsList) {
    String indexName = "by";
    for (String fieldName : fieldsList) {
      indexName += fieldName.replace("_", "").replace("-", "");
    }
    return indexName;
  }

  @Override
  public void setup(boolean forceSetup) throws Storage.StorageException {

    logger.debug("{} Setup connection {}, force {}", bucket.name(), this.id, forceSetup);

    List<List<String>> indexFields = getConfiguration().couchbase.bucketsIndex.getOrDefault(this.id, new ArrayList());

    // @TODO: find a way to query N1QL to check if index exists
    if (forceSetup) {

      logger.debug("{} Drop primary index", bucket.name());

      N1qlQueryResult result = bucket.query(N1qlQuery.simple(
              Index.dropPrimaryIndex(bucket.name())
      ));
      
      checkErrors(result, 5000); // ignore {"msg":"GSI index #primary not found.","code":5000}
      
      if (!indexFields.isEmpty()) {
        for (List<String> fieldsList : indexFields) {

          String indexName = getIndexName(fieldsList);

          logger.debug("{} Drop secondary index {}", bucket.name(), indexName);
          result = bucket.query(N1qlQuery.simple(
                  Index.dropIndex(bucket.name(), indexName)
          ));
          
          checkErrors(result, 5000); // ignore {"msg":"GSI index #primary not found.","code":5000}
          
        }
      }

      logger.debug("{} Create primary index", bucket.name());
      bucket.query(N1qlQuery.simple(
              Index.createPrimaryIndex().on(bucket.name())
      ));

      if (!indexFields.isEmpty()) {
        for (List<String> fieldsList : indexFields) {

          String fieldsNames = "";

          String indexName = getIndexName(fieldsList);
          for (String fieldName : fieldsList) {
            fieldsNames += "`" + fieldName + "`,";
          }

          String indexQuery = "CREATE INDEX `" + indexName
                  + "` ON `" + bucket.name()
                  + "` (" + fieldsNames.substring(0, fieldsNames.length() - 1) + ")";

          logger.debug("{} Create secondary index {}", bucket.name(), indexName);
          logger.debug("{} N1QL query: {}", bucket.name(), indexQuery);

          result = bucket.query(N1qlQuery.simple(indexQuery));

          checkErrors(result, 5000); // ignore  {"msg":"GSI CreateIndex() - cause: Index userIdobjectId already exist.","code":5000}

        }
      }

    }
  }

  private void checkErrors(N1qlQueryResult result, int skipCode) throws Storage.StorageException {
    if (!result.errors().isEmpty()) {
      String errors = "[ ";
      List<JsonObject> errorsList = result.errors();
      for (JsonObject err : errorsList) {
        
        int code = err.getInt("code");
        if(errorsList.size() ==1 && (code != 0 && code == skipCode)) {
          logger.warn("{} Ignored error on index setup: {}", bucket.name(), err.toString());
          return;
        }
        
        errors += err.toString() + ", ";
      }
      errors += "]";
      throw new Storage.StorageException("Error on index for " + bucket.name() + ": " + errors);
    }
  }

}
