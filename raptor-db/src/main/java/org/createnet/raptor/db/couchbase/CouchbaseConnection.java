/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.db.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.Index;
import com.couchbase.client.java.query.N1qlParams;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.Select;
import com.couchbase.client.java.query.Statement;
import com.couchbase.client.java.query.consistency.ScanConsistency;
import com.couchbase.client.java.query.dsl.Expression;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.createnet.raptor.db.AbstractConnection;
import org.createnet.raptor.db.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class CouchbaseConnection extends AbstractConnection {
  
  private final Logger logger = LoggerFactory.getLogger(CouchbaseConnection.class);
  
  final String indexPrefix = "by";
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
  public void connect() {
  }

  @Override
  public void set(String id, String data, int ttlSeconds) {
    JsonObject obj = JsonObject.fromJson(data);
    JsonDocument doc = JsonDocument.create(id, ttlSeconds, obj);
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
    bucket.remove(id);
  }

  @Override
  public List<String> list(String key, String value) throws Storage.StorageException {

    Statement select = Select.select("*").from(bucket.name())
            .where(Expression.x(key).eq("`" + value + "`"));
    N1qlParams ryow = N1qlParams.build().consistency(ScanConsistency.REQUEST_PLUS);

    N1qlQueryResult results = bucket.query(N1qlQuery.simple(select, ryow));
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
      list.add(row.value().toString());
    }

    return list;
  }

  @Override
  public void setup(boolean forceSetup) throws Storage.StorageException {
    
    logger.debug("Setup connection {}, force {}", this.id , forceSetup);
    
    List<List<String>> indexFields = getConfiguration().couchbase.bucketsIndex.getOrDefault(this.id, new ArrayList());

    if (forceSetup) {
      
      logger.debug("Drop primary index");
      
      bucket.query(N1qlQuery.simple(
              Index.dropPrimaryIndex(bucket.name())
      ));

      if (!indexFields.isEmpty()) {
        for (List<String> fieldsList : indexFields) {

          String indexName = indexPrefix;
          for (String fieldName : fieldsList) {
            indexName += fieldName;
          }
          
          logger.debug("Drop secondary index {}", indexName);
          bucket.query(N1qlQuery.simple(
                  Index.dropIndex(bucket.name(), indexName)
          ));

        }
      }

    }

    logger.debug("Create primary index");
    bucket.query(N1qlQuery.simple(
            Index.createPrimaryIndex().on(bucket.name())
    ));

    if (!indexFields.isEmpty()) {
      for (List<String> fieldsList : indexFields) {
                
        String fieldsNames = "";
        String indexName = indexPrefix;
        for (String fieldName : fieldsList) {
          indexName += fieldName;
          fieldsNames += "`" + fieldName + "`,";
        }
        
        String indexQuery = "CREATE INDEX `"+ indexName 
                +"` ON `"+ bucket.name()
                +"` ("+ fieldsNames.substring(0, fieldsNames.length()-1) +")";

        logger.debug("Create secondary index {}", indexName);
        logger.debug("N1QL query: {}", indexQuery);
        
         N1qlQueryResult result = bucket.query(N1qlQuery.simple(indexQuery));
         
         if(!result.errors().isEmpty()) {
           String errors = "";
           for(JsonObject err : result.errors()) {
             errors += "\n- " + err.toString();
           }
           
           throw new Storage.StorageException("Error creating index for " + bucket.name() + ": " + errors);
         }
         
         
         
      }
    }

  }

}
