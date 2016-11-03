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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.createnet.raptor.events.Event;
import org.createnet.raptor.events.type.ObjectEvent;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.Channel;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.search.query.impl.es.ObjectQuery;
import org.createnet.raptor.service.AbstractRaptorService;
import org.createnet.raptor.service.exception.ObjectNotFoundException;
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
public class ObjectManagerService extends AbstractRaptorService {

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

    private final static Logger logger = LoggerFactory.getLogger(ObjectManagerService.class);

    /**
     * Load an object definition by ID, without checking for permissions
     *
     * @param id object id to load
     * @return object definition
     */
    public ServiceObject load(String id) {

        logger.debug("Load object {}", id);

        List<ServiceObject> objs = indexer.getObjects(Arrays.asList(id));
        if (objs.isEmpty()) {
            throw new ObjectNotFoundException("Object " + id + " not found");
        }

        logger.debug("Loaded object {}", id);
        return objs.get(0);
    }

    /**
     * Return a list of definitions a user own
     *
     * @param userId id of the owning user
     * @param offset offset to start from
     * @param limit limit of record per call
     * @return the list of objects
     */
    public List<ServiceObject> list(String userId, Integer offset, Integer limit) {

        List<ServiceObject> list = indexer.getObjectsByUser(
                userId, offset, limit
        );

        // TODO: Add to list device which access is allowed
        return list;
    }

    private List<Stream> getChangedStreams(ServiceObject storedObj, ServiceObject obj) {

        List<Stream> changedStream = new ArrayList();

        // loop previous stream list and find missing streams
        for (Map.Entry<String, Stream> item : storedObj.streams.entrySet()) {

            String streamName = item.getKey();
            Stream stream = item.getValue();

            // stream found
            if (obj.streams.containsKey(streamName)) {

                // loop stream and find changed channels
                for (Map.Entry<String, Channel> channelItem : stream.channels.entrySet()) {

                    String channelName = channelItem.getKey();
                    Channel channel = channelItem.getValue();

                    if (storedObj.streams.get(streamName).channels.containsKey(channelName)) {
                        // check if channel definition changed
                        if (!storedObj.streams.get(streamName).channels.get(channelName).type.equals(channel.type)) {
                            changedStream.add(stream);
                            break;
                        }
                    } else {
                        // channel has gone, drop stream
                        changedStream.add(stream);
                        break;
                    }

                }
            } else {
                // drop stream
                changedStream.add(stream);
                storedObj.streams.remove(streamName);
            }

        }

        return changedStream;
    }

    private List<Action> getChangedActions(ServiceObject storedObj, ServiceObject obj) {
        List<Action> changedAction = new ArrayList();
        obj.actions.values().stream().filter((action) -> (!storedObj.actions.containsKey(action.name))).forEachOrdered((action) -> {
            changedAction.add(action);
        });
        return changedAction;
    }

    /**
     * Creates a new object and store it
     *
     * @param obj the object definition to create
     * @return the create object
     */
    public ServiceObject create(ServiceObject obj) {

        obj.id = null;

        storage.saveObject(obj);
        indexer.saveObject(obj, true);

        emitter.trigger(Event.EventName.create, new ObjectEvent(obj));

        logger.debug("Created new object {} for {}", obj.id, obj.userId);

        return obj;
    }

    /**
     * Updates a object definition, removing data from stream and action
     * accordingly
     *
     * @param obj
     * @return
     */
    public ServiceObject update(ServiceObject obj) {

        ServiceObject storedObj = load(obj.id);

        logger.debug("Updating object {}", obj.id);

        // @TODO: handle proper object update, ensuring stream data integrity
        storedObj.customFields.clear();
        storedObj.customFields.putAll(obj.customFields);

        // update settings
        storedObj.settings.storeData = obj.settings.storeData;
        storedObj.settings.eventsEnabled = obj.settings.eventsEnabled;

        // merge stream definitions
        List<Stream> changedStreams = getChangedStreams(storedObj, obj);

        storedObj.streams.clear();
        storedObj.addStreams(obj.streams.values());

        // merge action definitions
        List<Action> changedActions = getChangedActions(storedObj, obj);

        storedObj.actions.clear();
        storedObj.addActions(obj.actions.values());

        storage.saveObject(storedObj);
        indexer.saveObject(storedObj, false);

        // clean up data for changed stream and actions
        storage.deleteData(changedStreams);
        indexer.deleteData(changedStreams);
        storage.deleteActionStatus(changedActions);

        emitter.trigger(Event.EventName.update, new ObjectEvent(storedObj));

        logger.debug("Updated object {} for {}", storedObj.id, obj.userId);

        return obj;
    }

    /**
     * Delete an object definition
     *
     * @param id the id of the object to delete
     */
    public void delete(String id) {

        ServiceObject obj = load(id);

        storage.deleteObject(obj);
        indexer.deleteObject(obj);

        emitter.trigger(Event.EventName.delete, new ObjectEvent(obj));

        logger.debug("Deleted object {}", id);

    }

    /**
     * Search for objects
     *
     * @param query Query parameters to match in the definition
     * @return the matching definitions
     */
    public List<ServiceObject> search(ObjectQuery query) {
        List<ServiceObject> list = indexer.searchObject(query);
        return list;
    }

    /**
     * Return the object tree
     *
     * @param obj the object to load the tree from
     * @return the tree of definition
     */
    public List<ServiceObject> tree(ServiceObject obj) {
        return tree.getChildren(obj);
    }

    /**
     * Set the children of an object
     *
     * @param parentObject parent object
     * @param childrenObjects list of object to set as children
     * @return the list of children
     */
    public List<ServiceObject> setChildren(ServiceObject parentObject, List<ServiceObject> childrenObjects) {

        List<ServiceObject> list = tree.setChildren(parentObject, childrenObjects);

        // @TODO: Move to event emitter
        List<ServiceObject> toSave = new ArrayList(list);
        toSave.add(parentObject);

        storage.saveObjects(toSave);

        return list;
    }

    public List<ServiceObject> addChildren(ServiceObject parentObject, List<ServiceObject> childObjects) {

        List<ServiceObject> list = tree.addChildren(parentObject, childObjects);

        // @TODO: Move to event emitter
        List<ServiceObject> toSave = new ArrayList(list);
        toSave.add(parentObject);
        storage.saveObjects(toSave);

        return list;
    }

    public List<ServiceObject> removeChildren(ServiceObject parentObject, List<ServiceObject> childObjects) {

        List<ServiceObject> list = tree.removeChildren(parentObject, childObjects);

        // @TODO: Move to event emitter
        List<ServiceObject> toSave = new ArrayList(list);
        toSave.add(parentObject);
        storage.saveObjects(toSave);
        
        return list;
    }
    
}
