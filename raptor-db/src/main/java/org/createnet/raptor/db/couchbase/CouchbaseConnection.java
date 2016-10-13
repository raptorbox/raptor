/*
 * Copyright 2016 CREATE-NET.
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
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
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
    public void set(String id, JsonNode data, int ttlDays) {

        JsonObject obj = JsonObject.fromJson(data.toString());

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

    public void set(String id, JsonNode data) {
        set(id, data, 0);
    }

    @Override
    public JsonNode get(String id) {
        JsonDocument doc = bucket.get(id);
        if (doc == null) {
            return null;
        }
        try {
            return Storage.mapper.readTree(doc.content().toString());
        } catch (IOException ex) {
            logger.error("Cannot parse response JSON: {}", ex.getMessage());
            return null;
        }
    }

    @Override
    public void delete(String id) {
        try {
            bucket.remove(id);
        } catch (DocumentDoesNotExistException ex) {
            logger.debug("Cannot remove doc, doesn't exists {}", id);
        }

    }

    @Override
    public List<JsonNode> list(ListQuery query) {

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

        List<JsonNode> list = new ArrayList();
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
            String raw = row.value().get(bucket.name()).toString();
            JsonNode node;
            try {
                node = Storage.mapper.readTree(raw);
                list.add(node);
            } catch (IOException ex) {
                logger.warn("Cannot parse record: {}", raw);
                continue;
            }
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
    public void setup(boolean forceSetup) {

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

    private void checkErrors(N1qlQueryResult result, int skipCode) {
        if (!result.errors().isEmpty()) {
            String errors = "[ ";
            List<JsonObject> errorsList = result.errors();
            for (JsonObject err : errorsList) {

                int code = err.getInt("code");
                if (errorsList.size() == 1 && (code != 0 && code == skipCode)) {
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
