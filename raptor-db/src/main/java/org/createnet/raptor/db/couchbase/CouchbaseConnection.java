/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.db.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
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
    if(doc == null) return null;
    return doc.content().toString();
  }

  @Override
  public void delete(String id) {
    bucket.remove(id);
  }

}
