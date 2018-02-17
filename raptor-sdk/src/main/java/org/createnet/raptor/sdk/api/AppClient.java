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
package org.createnet.raptor.sdk.api;

import org.createnet.raptor.sdk.Routes;
import org.createnet.raptor.sdk.AbstractClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.createnet.raptor.models.app.App;
import org.createnet.raptor.models.payload.AppPayload;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.exception.ClientException;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.createnet.raptor.models.query.AppQuery;
import org.createnet.raptor.sdk.PageResponse;
import org.createnet.raptor.sdk.QueryString;
import org.createnet.raptor.sdk.events.callback.AppCallback;
import org.createnet.raptor.sdk.events.callback.AppEventCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods to interact with Raptor API
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class AppClient extends AbstractClient {

    public AppClient(Raptor container) {
        super(container);
    }

    final static Logger logger = LoggerFactory.getLogger(AppClient.class);

    /**
     * Register for app events
     *
     * @param callback The callback to fire on event arrival
     */
    public void subscribe(App app, AppEventCallback callback) {
        getEmitter().subscribe(app, callback);
    }

    /**
     * Subscribe only to app related events like update or delete
     *
     * @param ev
     */
    public void subscribe(App app, AppCallback ev) {
        getEmitter().subscribe(app, (DispatcherPayload payload) -> {
            switch (payload.getType()) {
                case app:
                    ev.callback(app, (AppPayload) payload);
                    break;
            }
        });
    }

    /**
     * Create a new app instance
     *
     * @param obj app definition to create
     * @return the App instance
     */
    public App create(App obj) {
        
        JsonNode node = getClient().post(Routes.APP_CREATE, toJsonNode(obj));
        App rapp = getMapper().convertValue(node, App.class);
        
        if (!node.has("id")) {
            throw new ClientException("Missing ID on object creation");
        }
        
        obj.merge(rapp);
        obj.setId(rapp.getId());

        return obj;
    }

    /**
     * Load an app definition
     *
     * @param id unique id of the app
     * @return the App instance
     */
    public App load(String id) {
        App app = getMapper().convertValue(getClient().get(String.format(Routes.APP_READ, id)), App.class);
        return app;
    }

    /**
     * Update a app instance
     *
     * @param obj the App to update
     * @return the updated App instance
     */
    public App update(App obj) {
        JsonNode n = getClient().put(
                String.format(Routes.APP_UPDATE, obj.getId()),
                toJsonNode(obj)
        );
        obj.merge(getMapper().convertValue(n, App.class));
        return obj;
    }

    /**
     * Delete a App instance and all of its data
     *
     * @param obj App to delete
     */
    public void delete(App obj) {
        getClient().delete(
                String.format(Routes.APP_DELETE, obj.getId())
        );
        obj.setId(null);
    }

    /**
     * List accessible app
     *
     * @return the App instance
     */
    public PageResponse<App> list() {
        JsonNode json = getClient().get(Routes.APP_LIST);
        PageResponse<App> list = getMapper().convertValue(json, new TypeReference<PageResponse<App>>() {});
        return list;
//        return list(1, 25);
    }
    
    /**
     * List accessible app
     *
     * @return the App instance
     */
    public PageResponse<App> list(int page, int limit) {
    	QueryString qs = new QueryString();
        qs.pager.page = page;
        qs.pager.size = limit;
        JsonNode json = getClient().get(Routes.APP_LIST + qs.toString());
        PageResponse<App> list = getMapper().convertValue(json, new TypeReference<PageResponse<App>>() {});
        return list;
    }

    public PageResponse<App> search(AppQuery q) {
        JsonNode json = getClient().post(Routes.APP_SEARCH, toJsonNode(q));
        PageResponse<App> list = getMapper().convertValue(json, new TypeReference<PageResponse<App>>() {});
        return list;
    }

}
