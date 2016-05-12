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
import org.createnet.raptor.db.AbstractConnection;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class CouchbaseConnection extends AbstractConnection {

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
  public List<String> list(String key, String value) {

    Statement select = Select.select("*").from(bucket.name())
            .where(Expression.x(key).eq(value));
    N1qlParams ryow = N1qlParams.build().consistency(ScanConsistency.REQUEST_PLUS);

    N1qlQueryResult results = bucket.query(N1qlQuery.simple(select, ryow));
    List<String> list = new ArrayList();

    Iterator<N1qlQueryRow> it = results.allRows().iterator();
    while (it.hasNext()) {
      N1qlQueryRow row = it.next();
      list.add(row.value().toString());
    }

    return list;
  }

  @Override
  public void setup(boolean forceSetup) {

    List<String> indexFields = getConfiguration().couchbase.bucketsIndex.getOrDefault(this.id, new ArrayList());

    if (forceSetup) {

      bucket.query(N1qlQuery.simple(
              Index.dropPrimaryIndex(bucket.name())
      ));

      if (!indexFields.isEmpty()) {
        for (String field : indexFields) {
          bucket.query(N1qlQuery.simple(
                  Index.dropIndex(bucket.name(), "by_" + field)
          ));
        }
      }

    }
    
    // create main index
    bucket.query(N1qlQuery.simple(
            Index.createPrimaryIndex().on(bucket.name())
    ));

    if (!indexFields.isEmpty()) {
      for (String field : indexFields) {
        bucket.query(N1qlQuery.simple(
                Index.createIndex("by_" + field).on(bucket.name(), Expression.x(field)))
        );
      }
    }

  }

}
