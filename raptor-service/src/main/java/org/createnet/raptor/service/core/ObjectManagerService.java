/*
 * Copyright 2017 FBK/CREATE-NET.
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.createnet.raptor.events.Event;
import org.createnet.raptor.events.type.ObjectEvent;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.Channel;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.indexer.query.impl.es.ObjectQuery;
import org.createnet.raptor.service.AbstractRaptorService;
import org.createnet.raptor.service.exception.DeviceNotFoundException;
import org.createnet.raptor.service.exception.DeviceOperationException;
import org.createnet.raptor.service.tools.DispatcherService;
import org.createnet.raptor.service.tools.EventEmitterService;
import org.createnet.raptor.service.tools.IndexerService;
import org.createnet.raptor.service.tools.StorageService;
import org.createnet.raptor.service.tools.TreeService;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.createnet.raptor.indexer.Indexer;

/**
 * API to manage Object definitions
 *
 * @author Luca Capra <luca.capra@fbk.eu>
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
    public Device load(String id) {

        logger.debug("Load object {}", id);

        List<Device> objs = indexer.getObjects(Arrays.asList(id));
        if (objs.isEmpty()) {
            throw new DeviceNotFoundException("Object " + id + " not found");
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
    public List<Device> list(String userId, Integer offset, Integer limit) {

        List<Device> list = indexer.getObjectsByUser(
                userId, offset, limit
        );

        // TODO: Add to list device which access is allowed
        return list;
    }

    synchronized private List<Stream> getChangedStreams(Device storedObj, Device obj) {

        List<Stream> changedStream = new ArrayList();

        // loop previous stream list and find missing streams
        for (Iterator<Stream> it = storedObj.getStreams().values().iterator(); it.hasNext();) {

            Stream stream = it.next();
            // stream found
            if (obj.streams.containsKey(stream.name)) {

                // loop stream and find changed channels
                for (Map.Entry<String, Channel> channelItem : stream.channels.entrySet()) {

                    String channelName = channelItem.getKey();
                    Channel channel = channelItem.getValue();

                    if (storedObj.streams.get(stream.name).channels.containsKey(channelName)) {
                        // check if channel definition changed
                        if (!storedObj.streams.get(stream.name).channels.get(channelName).type.equals(channel.type)) {
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
                it.remove();
            }
        }

        return changedStream;
    }

    synchronized private List<Action> getChangedActions(Device storedObj, Device obj) {
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
    public Device create(Device obj) {

        obj.id = null;

        storage.saveObject(obj);
        try {
            indexer.saveObject(obj, true);
        } catch (Indexer.IndexerException ex) {

            logger.error("Indexing error occured", ex);
            logger.warn("Removing object {} from storage", obj.id);

            storage.deleteObject(obj);

            throw new DeviceOperationException("Failed to index device");
        }

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
    public Device update(Device obj) {
        
        // ensure incoming obj is valid
        obj.validate();
        
        Device storedObj = load(obj.id);

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
        
        if(obj.name != null)
            storedObj.name = obj.name;
        
        if(obj.description != null)
            storedObj.description = obj.description;
        
        storedObj.settings.eventsEnabled = obj.settings.eventsEnabled;
        storedObj.settings.storeData = obj.settings.storeData;
        
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

        Device obj = load(id);

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
    public List<Device> search(ObjectQuery query) {
        List<Device> list = indexer.searchObject(query);
        return list;
    }

    /**
     * Return the object tree
     *
     * @param obj the object to load the tree from
     * @return the tree of definition
     */
    public List<Device> tree(Device obj) {
        return tree.getChildren(obj);
    }

    /**
     * Set the children of an object
     *
     * @param parentObject parent object
     * @param childrenObjects list of object to set as children
     * @return the list of children
     */
    public List<Device> setChildren(Device parentObject, List<Device> childrenObjects) {

        List<Device> list = tree.setChildren(parentObject, childrenObjects);

        // @TODO: Move to event emitter
        List<Device> toSave = new ArrayList(list);
        toSave.add(parentObject);

        storage.saveObjects(toSave);

        return list;
    }

    public List<Device> addChildren(Device parentObject, List<Device> childObjects) {

        List<Device> list = tree.addChildren(parentObject, childObjects);

        // @TODO: Move to event emitter
        List<Device> toSave = new ArrayList(list);
        toSave.add(parentObject);
        storage.saveObjects(toSave);

        return list;
    }

    public List<Device> removeChildren(Device parentObject, List<Device> childObjects) {

        List<Device> list = tree.removeChildren(parentObject, childObjects);

        // @TODO: Move to event emitter
        List<Device> toSave = new ArrayList(list);
        toSave.add(parentObject);
        storage.saveObjects(toSave);
        
        return list;
    }
    
}
