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

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import org.createnet.raptor.service.tools.AuthService;
import org.createnet.raptor.service.tools.DispatcherService;
import org.createnet.raptor.service.tools.EventEmitterService;
import org.createnet.raptor.service.tools.IndexerService;
import org.createnet.raptor.service.tools.StorageService;
import org.createnet.raptor.service.tools.TreeService;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.service.core.ObjectManagerService;
import org.createnet.raptor.service.core.StreamManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
abstract public class AbstractApi {

    final private Logger logger = LoggerFactory.getLogger(AbstractApi.class);
    
    @Inject
    protected ObjectManagerService objectManager;
    
    @Inject
    protected StreamManagerService streamManager;
    
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

    protected Action loadAction(String actionId, ServiceObject obj) {

        Action action = obj.actions.getOrDefault(actionId, null);

        if (action == null) {
            throw new NotFoundException("Action " + actionId + "not found");
        }

        return action;
    }
 
    
}
