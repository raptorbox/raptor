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
package org.createnet.raptor.service.core;

import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import org.createnet.raptor.events.Event;
import org.createnet.raptor.events.type.ActionEvent;
import org.createnet.raptor.events.type.DataEvent;
import org.createnet.raptor.models.data.ActionStatus;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.indexer.query.impl.es.DataQuery;
import org.createnet.raptor.service.AbstractRaptorService;
import org.createnet.raptor.service.exception.ActionNotFoundException;
import org.createnet.raptor.service.exception.StreamNotFoundException;
import org.createnet.raptor.service.tools.DispatcherService;
import org.createnet.raptor.service.tools.EventEmitterService;
import org.createnet.raptor.service.tools.IndexerService;
import org.createnet.raptor.service.tools.StorageService;
import org.createnet.raptor.service.tools.TreeService;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API to manage Object definitions
 *
 * @author Luca Capra <luca.capra@create-net.org>
 */
@Service
public class ActionManagerService extends AbstractRaptorService {

    @Inject
    protected ObjectManagerService objectManager;

    @Inject
    protected EventEmitterService emitter;

    @Inject
    protected StorageService storage;

    @Inject
    protected IndexerService indexer;

    @Inject
    protected DispatcherService dispatcher;

    @Inject
    protected TreeService tree;

    private final static Logger logger = LoggerFactory.getLogger(ActionManagerService.class);

    /**
     * Load a stream based on object id and its name
     *
     * @param objectId id of the objectId
     * @param actionId name of the action
     * @return a Stream instance
     */
    public Action load(String objectId, String actionId) {

        // just an effort to ensure arguments are not flipped
        assert objectId.length() == 36;

        ServiceObject obj = objectManager.load(objectId);
        Action action = obj.actions.getOrDefault(actionId, null);

        if (action == null) {
            throw new ActionNotFoundException("Action " + actionId + " not found");
        }

        return action;

    }

    /**
     * Load the status of an action if available
     *
     * @param action the reference action
     * @return status of that action
     */
    public ActionStatus getStatus(Action action) {

        assert action.getServiceObject() != null;

        logger.debug("Fetched action {} status for object {}", action.name, action.getServiceObject().id);
        return storage.getActionStatus(action);
    }

    public ActionStatus setStatus(Action action, String body) {

        assert action.getServiceObject() != null;

        ActionStatus actionStatus = storage.saveActionStatus(action, body);

        emitter.trigger(Event.EventName.execute, new ActionEvent(action, actionStatus));

        logger.debug("Saved action {} status for object {}", action.name, action.getServiceObject().id);

        return actionStatus;
    }

    public void removeStatus(Action action) {
        
        assert action.getServiceObject() != null;
        
        storage.deleteActionStatus(action);
        emitter.trigger(Event.EventName.deleteAction, new ActionEvent(action, null));

        logger.debug("removed action {} status for object {}", action.name, action.getServiceObject().id);
    }

}
