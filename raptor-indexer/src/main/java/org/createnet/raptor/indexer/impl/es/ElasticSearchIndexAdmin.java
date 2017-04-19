/*
 * Copyright 2017 FBK/CREATE-NET
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
package org.createnet.raptor.indexer.impl.es;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.createnet.raptor.indexer.Indexer;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class ElasticSearchIndexAdmin {

    public class IndexAdminException extends RuntimeException {

        public IndexAdminException(String message) {
            super(message);
        }

        public IndexAdminException(Throwable cause) {
            super(cause);
        }

    }

    final protected Logger logger = LoggerFactory.getLogger(ElasticSearchIndexAdmin.class);
    private Client client;

    public Client getClient() throws IndexAdminException {
        if (client == null) {
            throw new IndexAdminException("ES client must be set");
        }
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public boolean exists(String name) {
        Future<IndicesExistsResponse> req = getClient().admin().indices().exists(new IndicesExistsRequest(name));
        IndicesExistsResponse res;
        try {
            res = req.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new IndexAdminException(ex);
        }
        logger.debug("Index {} does {} exists", name, res.isExists() ? "" : "not");
        return res.isExists();
    }

    public void delete(String name) {

        DeleteIndexRequest deleteIndexReq = new DeleteIndexRequest(name);
        Future<DeleteIndexResponse> reqCreate = client.admin().indices().delete(deleteIndexReq);
        try {
            DeleteIndexResponse resDelete = reqCreate.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new IndexAdminException(ex);
        }

        logger.debug("Delete index {}", name);
    }

    public void create(String name, JsonNode definition) {

        try {
            
            logger.debug("Creating index {}: {}", name, definition.toString());
            
            CreateIndexRequestBuilder reqb = client.admin().indices().prepareCreate(name);
            
            if (definition.has("settings")) {
                reqb.setSettings(
                    Indexer.getObjectMapper().writeValueAsString(definition.get("settings"))
                );
            }
            
            Iterator<Map.Entry<String, JsonNode>> nodes = definition.get("mappings").fields();
            while (nodes.hasNext()) {
                Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodes.next();
                reqb.addMapping(entry.getKey(), Indexer.getObjectMapper().writeValueAsString(entry.getValue()));
            }
                
            reqb.get();
            
            
        } catch (Exception ex) {
            throw new IndexAdminException(ex);
        }

        logger.debug("Created index {}", name);
    }

}
