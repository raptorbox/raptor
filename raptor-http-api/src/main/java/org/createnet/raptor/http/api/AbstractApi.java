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
package org.createnet.raptor.http.api;

import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.http.service.AuthService;
import org.createnet.raptor.http.service.DispatcherService;
import org.createnet.raptor.http.service.EventEmitterService;
import org.createnet.raptor.http.service.IndexerService;
import org.createnet.raptor.http.service.StorageService;
import org.createnet.raptor.http.service.TreeService;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
abstract public class AbstractApi {

    final private Logger logger = LoggerFactory.getLogger(AbstractApi.class);

    @Inject
    protected EventEmitterService emitter;

    @Inject
    protected StorageService storage;

    @Inject
    protected IndexerService indexer;

    @Inject
    protected AuthService auth;
    
    @Inject
    protected DispatcherService dispatcher;

    @Inject
    protected TreeService tree;

    protected ServiceObject loadObject(String id) {

        logger.debug("Load object {}", id);
        List<ServiceObject> objs = indexer.getObjects(Arrays.asList(id));
        if (objs.isEmpty()) {
            throw new NotFoundException("Object " + id + " not found");
        }

        ServiceObject obj = objs.get(0);

        return obj;
    }

    protected Stream loadStream(String streamId, ServiceObject obj) {

        Stream stream = obj.streams.getOrDefault(streamId, null);

        if (stream == null) {
            throw new NotFoundException("Stream " + streamId + " not found");
        }

        return stream;
    }

    protected Action loadAction(String actionId, ServiceObject obj) {

        Action action = obj.actions.getOrDefault(actionId, null);

        if (action == null) {
            throw new NotFoundException("Action " + actionId + "not found");
        }

        return action;
    }

    protected boolean syncObject(ServiceObject obj, Authentication.SyncOperation op) {
        try {
            auth.sync(auth.getAccessToken(), obj, op);
        } catch (Exception ex) {
            logger.error("Error syncing object to auth system: {}", ex.getMessage());
            return false;
        }
        return true;
    }    
    
}
