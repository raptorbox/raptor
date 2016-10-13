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
package org.createnet.raptor.db.mapdb;

import com.couchbase.client.deps.com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.db.AbstractConnection;
import org.createnet.raptor.db.query.ListQuery;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
class MapDBConnection extends AbstractConnection {

    protected static final String keySeparator = "_";

    private final Logger logger = LoggerFactory.getLogger(MapDBConnection.class);

    static private DB db;
    private HTreeMap<String, String> map;
    private HTreeMap<String, String> configStore;
    private final Map<String, BTreeMap<String, String>> indexMap = new HashMap();

    static public class Record {

        public long ttl;
        public JsonNode content;
        public String id;

        private Record() {
        }

        private Record(String id, JsonNode content, long ttl) {
            this.id = id;
            this.content = content;
            this.ttl = ttl;
        }

        public JsonNode toJsonNode() {
            return Storage.mapper.convertValue(this, JsonNode.class);
        }

        @Override
        public String toString() {
            return toJsonNode().toString();
        }

    }

    public MapDBConnection(Storage.ConnectionId connId) {
        this.id = connId.name();
    }

    @Override
    public void setup(boolean forceSetup) {

        if (db != null) {
            return;
        }

        if (configuration.mapdb.storage.equals("memory")) {
            db = DBMaker.memoryDB().make();
        } else {

            File file = new File(configuration.mapdb.storePath + File.separator + this.id + ".mapdb");

            if (forceSetup && file.exists()) {
                file.delete();
            }

            try {

                db = DBMaker
                        .fileDB(file)
                        .closeOnJvmShutdown()
                        .closeOnJvmShutdownWeakReference()
                        //            .checksumStoreEnable()
                        //            .checksumHeaderBypass()
                        .transactionEnable()
                        .make();

            } catch (Exception e) {
                logger.error("Failed to open db at {}: {}", file.getPath(), e.getMessage());
                throw new Storage.StorageException(e);
            }

            logger.debug("Loaded store for {} at {}", this.id, file.getPath());
        }

        setupIndexes(forceSetup);

    }

    @Override
    public void connect() {

        map = db.hashMap(this.id + "_dataset")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.STRING)
                .createOrOpen();

//    ttl = db.treeMap(this.id + "_ttl")
//            .keySerializer(Serializer.LONG)
//            .valueSerializer(Serializer.STRING)
//            .createOrOpen();
    }

    @Override
    public void disconnect() {
        if (db != null) {
            db.close();
        }
    }

    @Override
    public void set(String id, JsonNode data, int ttlDays) {
        Record r = new Record(id, data, (long) ttlDays);
//    ttl.put(r.ttl, r.id);
        map.put(r.id, r.toString());
        addToIndex(r);
        db.commit();
    }

    @Override
    public JsonNode get(String id) {

        Record r = getRecord(id);

        if (r == null) {
            return null;
        }

        if (r.ttl > 0 && Instant.ofEpochSecond(r.ttl).isBefore(Instant.now())) {
            delete(id);
            return null;
        }

        return r.content;
    }

    public Record getRecord(String id) {
        String content = map.get(id);
        if (content == null) {
            return null;
        }

        return parseRecord(content);
    }

    @Override
    public List<JsonNode> list(ListQuery query) {

        List<JsonNode> results = new ArrayList();

        List<ListQuery.QueryParam> params = query.getParams();

        if (params.size() > 0) {

            List<String> keys = new ArrayList();
            Map<String, String> values = new HashMap();

            for (ListQuery.QueryParam param : params) {
                keys.add(param.key);
                values.put(param.key, param.value.toString());
            }

            String key = getIndexKey(keys);

            String[] arrKeys = new String[keys.size()];
            arrKeys = keys.toArray(arrKeys);
            Arrays.sort(arrKeys);

            String[] keyVal = new String[arrKeys.length];
            int i = 0;
            for (String arrKey : arrKeys) {
                keyVal[i] = values.get(arrKey);
                i++;
            }

            String valuesKey = String.join(keySeparator, keyVal);

            BTreeMap<String, String> idx = indexMap.get(key);

            if (idx == null) {
                logger.debug("Missing index for {}", key);
                throw new Storage.StorageException("Missing index " + key);
            }

            String json = idx.get(valuesKey);

            // no results
            if (json == null) {
                return results;
            }

            try {

                String[] ids = Storage.mapper.readValue(json, String[].class);

                for (String id1 : ids) {
                    JsonNode row = get(id1);
                    if (row != null) {
                        results.add(row);
                    }
                }

            } catch (IOException ex) {
                throw new Storage.StorageException(ex);
            }

        }

        if (query.getLimit() > 0) {
        }

        if (query.getOffset() > 0) {
        }

        return results;
    }

    @Override
    public void delete(String id) {

        Record r = getRecord(id);
        if (r == null) {
            return;
        }

        map.remove(id);
//    ttl.remove(r.ttl);
    }

    protected Record parseRecord(String raw) {
        try {
            return Storage.mapper.readValue(raw, Record.class);
        } catch (IOException ex) {
            throw new Storage.StorageException(ex);
        }
    }

    private void setupIndexes(boolean forceSetup) {

        if (!configuration.mapdb.indices.containsKey(this.id)) {
            return;
        }

        configStore = db.hashMap(this.id + "_config")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.STRING)
                .createOrOpen();

        List<List<String>> indexDefinition = configuration.mapdb.indices.get(this.id);
        logger.debug("Setup index for {}", this.id);

        try {
            configStore.put("indexes", Storage.mapper.writeValueAsString(indexDefinition));
        } catch (JsonProcessingException ex) {
            throw new Storage.StorageException(ex);
        }

        for (List<String> keys : indexDefinition) {

            String key = getIndexKey(keys);

            if (!indexMap.containsKey(key)) {

                logger.debug("Adding index for {}", key);

                BTreeMap<String, String> idxmap = db.treeMap(String.format("%s_idx_%s", this.id, key))
                        .keySerializer(Serializer.STRING)
                        .valueSerializer(Serializer.STRING)
                        .createOrOpen();

                indexMap.put(key, idxmap);
            }

        }

        db.commit();

    }

    protected BTreeMap<String, String> getIndexMap(String key) {
        return indexMap.getOrDefault(key, null);
    }

    protected String getIndexKey(List<String> keys) {
        String[] arrKeys = new String[keys.size()];
        arrKeys = keys.toArray(arrKeys);
        Arrays.sort(arrKeys);
        return String.join(keySeparator, arrKeys);
    }

    protected String getIndexKeyVal(List<String> keys, Record r) {
        String[] arrKeys = new String[keys.size()];
        arrKeys = keys.toArray(arrKeys);
        Arrays.sort(arrKeys);
        String[] keyVal = new String[arrKeys.length];
        int i = 0;
        for (String arrKey : arrKeys) {
            if (!r.content.has(arrKey)) {
                logger.debug("Missing record key {} for record {}", arrKey, r.id);
                throw new Storage.StorageException("Cannot index record, missing key " + arrKey);
            }
            keyVal[i] = r.content.get(arrKey).asText();
            i++;
        }

        return String.join(keySeparator, keyVal);
    }

    protected void addToIndex(Record r) {

        List<List<String>> indexDefinition = configuration.mapdb.indices.get(this.id);
        for (List<String> keys : indexDefinition) {

            String keyhash = getIndexKey(keys);
            String valhash = getIndexKeyVal(keys, r);
            BTreeMap<String, String> idx = getIndexMap(keyhash);
            List<String> list = new ArrayList();
            String json;

            if (idx == null) {
                logger.debug("Skipped indexing for key: {}", keyhash);
                return;
            }

            json = idx.get(valhash);

            if (json != null) {
                try {
                    list = new ArrayList(Arrays.asList(Storage.mapper.readValue(json, String[].class)));
                } catch (IOException ex) {
                    throw new Storage.StorageException(ex);
                }
            }

            if (list.contains(r.id)) {
                // duplicate found, abort!
                continue;
            }

            list.add(r.id);

            try {
                json = Storage.mapper.writeValueAsString(list);
                idx.put(valhash, json);
            } catch (JsonProcessingException ex) {
                throw new Storage.StorageException(ex);
            }

        }

    }

}
