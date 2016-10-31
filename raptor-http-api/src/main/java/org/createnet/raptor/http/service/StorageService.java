/*
 * Copyright 2016 Luca Capra <lcapra@create-net.org>.
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
package org.createnet.raptor.http.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.createnet.raptor.models.objects.ServiceObject;
import org.jvnet.hk2.annotations.Service;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.db.StorageProvider;
import org.createnet.raptor.http.configuration.StorageConfiguration;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.createnet.raptor.models.data.ActionStatus;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.Stream;
import org.mapdb.DBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Singleton
@Service
public class StorageService extends AbstractRaptorService{

    /**
     * Maximum duration time for a record in the database eg. 90 days
     */
    private final int defaultDataTTL = 90;

    private final Logger logger = LoggerFactory.getLogger(StorageService.class);

    @Inject
    ConfigurationService configuration;

    @Inject
    IndexerService indexer;

    private Storage storage;

    @PostConstruct
    @Override
    public void initialize() {
        try {
            logger.debug("Initializing storage");
            getStorage();
        } catch (Storage.StorageException | ConfigurationException | DBException e) {
            throw new ServiceException(e);
        }
    }

    @PreDestroy
    @Override
    public void shutdown() {
        try {
            getStorage().disconnect();
            storage = null;
        } catch (Storage.StorageException | ConfigurationException e) {
            throw new ServiceException(e);
        }
    }

    private enum ConnectionId {
        objects, data, subscriptions, actuations
    }

    public Storage getStorage() {

        if (storage == null) {
            storage = new StorageProvider();
            StorageConfiguration conf = configuration.getStorage();
            storage.initialize(conf);
            storage.setup(false);
            storage.connect();
        }

        return storage;
    }

    protected Storage.Connection getConnection(String name) {
        Storage.Connection conn = getStorage().getConnection(name);
        if (conn == null) {
            throw new Storage.StorageException("Cannot load connection for " + name);
        }
        return conn;
    }

    public Storage.Connection getObjectConnection() {
        return getConnection(ConnectionId.objects.toString());
    }

    public Storage.Connection getDataConnection() {
        return getStorage().getConnection(ConnectionId.data.toString());
    }

    public Storage.Connection getActionConnection() {
        return getStorage().getConnection(ConnectionId.actuations.toString());
    }

    public ServiceObject getObject(String id) {
        JsonNode json = getObjectConnection().get(id);
        if (json == null) {
            return null;
        }
        return ServiceObject.fromJSON(json);
    }

    public String saveObject(ServiceObject obj) {

        obj.validate();

        if (obj.id == null) {
            obj.id = ServiceObject.generateUUID();
        }

        JsonNode json = obj.toJsonNode();
        getObjectConnection().set(obj.id, json, 0);
        return obj.id;
    }

    public void saveObjects(List<ServiceObject> objs) {

        List<ServiceObject> saved = objs.stream().map((ServiceObject o) -> {
            try {
                saveObject(o);
                return o;
            } catch (Exception ex) {
                logger.error("Error saving {}: {}", o.id, ex.getMessage());
                return null;
            }
        })
                .filter(o -> o != null)
                .collect(Collectors.toList());

        if (saved.size() != objs.size()) {
            throw new Storage.StorageException("Failed storing objects");
        }

    }

    public void deleteObject(ServiceObject obj) {

        // cleanup data
        deleteData(obj.streams.values());
        deleteActionStatus(obj.actions.values());

        getObjectConnection().delete(obj.id);

    }

    // Data 
    protected String getDataId(Stream stream, RecordSet record) {
        return stream.getServiceObject().id + "-" + stream.name + "-" + record.getTimestamp().getTime();
    }

    public void saveData(Stream stream, RecordSet record) {

        record.setStream(stream);
        record.userId = stream.getServiceObject().getUserId();

        getDataConnection().set(getDataId(stream, record), record.toJsonNode(), defaultDataTTL);
    }

    public void deleteData(Stream stream, RecordSet record) {
        getDataConnection().delete(getDataId(stream, record));
    }

    public void deleteData(Stream stream) {

        List<RecordSet> records = indexer.getStreamData(stream);

        for (RecordSet record : records) {
            deleteData(stream, record);
        }

    }

    public void deleteData(Collection<Stream> streams) {

        if (streams.isEmpty()) {
            return;
        }

        //drop stream data
        for (Stream stream : streams) {
            logger.debug("Removing stream {} data for object {}", stream.name, stream.getServiceObject().id);
            deleteData(stream);
        }

    }

    // Actuations
    protected String getActionId(Action action) {
        return action.getServiceObject().id + "-" + action.name;
    }

    public ActionStatus getActionStatus(Action action) {

        JsonNode rawStatus = getActionConnection().get(getActionId(action));

        ActionStatus actionStatus = ActionStatus.parseJSON(rawStatus);

        return actionStatus;
    }

    public ActionStatus saveActionStatus(Action action, String status) {

        ActionStatus actionStatus = new ActionStatus(action, status);

        getActionConnection().set(getActionId(action), actionStatus.toJsonNode(), defaultDataTTL);

        return actionStatus;
    }

    public void deleteActionStatus(Action action) {
        getActionConnection().delete(getActionId(action));
    }

    public void deleteActionStatus(Collection<Action> changedActions) {
        if (!changedActions.isEmpty()) {
            // drop action data
            for (Action changedAction : changedActions) {
                logger.debug("Removing action {} data for object {}", changedAction.name, changedAction.getServiceObject().id);
                deleteActionStatus(changedAction);
            }
        }
    }

}
