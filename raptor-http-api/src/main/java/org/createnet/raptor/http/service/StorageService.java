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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.jvnet.hk2.annotations.Service;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.db.StorageProvider;
import org.createnet.raptor.http.configuration.StorageConfiguration;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.createnet.raptor.models.data.ActionStatus;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.exception.RecordsetException;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.search.raptor.search.Indexer;
import org.mapdb.DBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class StorageService implements RaptorService {

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
    public void initialize() throws ServiceException {
        try {
            getStorage();
        } catch (Storage.StorageException | ConfigurationException | DBException e) {
            throw new ServiceException(e);
        }
    }

    @PreDestroy
    @Override
    public void shutdown() throws ServiceException {
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

    public Storage getStorage() throws Storage.StorageException, ConfigurationException {

        if (storage == null) {
            logger.debug("Initializing storage instance");
            storage = new StorageProvider();
            StorageConfiguration conf = configuration.getStorage();
            storage.initialize(conf);
            storage.setup(false);
            storage.connect();
        }

        return storage;
    }

    protected Storage.Connection getConnection(String name) throws Storage.StorageException, ConfigurationException {
        Storage.Connection conn = getStorage().getConnection(name);
        if (conn == null) {
            throw new Storage.StorageException("Cannot load connection for " + name);
        }
        return conn;
    }

    public Storage.Connection getObjectConnection() throws ConfigurationException, Storage.StorageException {
        return getConnection(ConnectionId.objects.toString());
    }

    public Storage.Connection getDataConnection() throws ConfigurationException, Storage.StorageException {
        return getStorage().getConnection(ConnectionId.data.toString());
    }

    public Storage.Connection getActionConnection() throws ConfigurationException, Storage.StorageException {
        return getStorage().getConnection(ConnectionId.actuations.toString());
    }

    public ServiceObject getObject(String id) throws Storage.StorageException, RaptorComponent.ParserException, ConfigurationException {
        JsonNode json = getObjectConnection().get(id);
        if (json == null) {
            return null;
        }
        return ServiceObject.fromJSON(json);
    }

    public String saveObject(ServiceObject obj) throws ConfigurationException, Storage.StorageException, RaptorComponent.ParserException, RaptorComponent.ValidationException, Authentication.AuthenticationException {

        obj.validate();

        if (obj.id == null) {
            obj.id = ServiceObject.generateUUID();
        }

        JsonNode json = obj.toJsonNode();
        getObjectConnection().set(obj.id, json, 0);
        return obj.id;
    }

    public void saveObjects(List<ServiceObject> objs) throws ConfigurationException, Storage.StorageException, RaptorComponent.ParserException, RaptorComponent.ValidationException, Authentication.AuthenticationException {

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

    public void deleteObject(ServiceObject obj) throws ConfigurationException, Storage.StorageException, Authentication.AuthenticationException, IOException, Indexer.IndexerException, RecordsetException {

        // cleanup data
        deleteData(obj.streams.values());
        deleteActionStatus(obj.actions.values());

        getObjectConnection().delete(obj.id);

    }

    // Data 
    protected String getDataId(Stream stream, RecordSet record) {
        return stream.getServiceObject().id + "-" + stream.name + "-" + record.getLastUpdate().getTime();
    }

//  public ResultSet fetchData(Stream stream) throws RecordsetException, ConfigurationException, Storage.StorageException, Authentication.AuthenticationException {
//    return fetchData(stream, defaultRecordLimit, 0);
//  }
//
//  public ResultSet fetchData(Stream stream, int limit, int offset) throws RecordsetException, ConfigurationException, Storage.StorageException, Authentication.AuthenticationException {
//
//    ResultSet resultset = new ResultSet(stream);
//
//    BaseQuery query = BaseQuery.queryBy("userId", auth.getUser().getUserId());
//    query.params.add(new ListQuery.QueryParam("streamId", stream.name));
//    query.params.add(new ListQuery.QueryParam("objectId", stream.getServiceObject().id));
//
//    query.setSort("lastUpdate", ListQuery.Sort.DESC);
//    
//    if(offset > 0)
//      query.offset = offset;
//    
//    if (limit > 0) {
//      query.limit = limit > defaultRecordLimit ? defaultRecordLimit : limit;
//    }
//
//    List<JsonNode> results = getDataConnection().list(query);
//
//    long parseError = 0;
//    RecordsetException lastException = null;
//    JsonNode lastRecord = null;
//    for (JsonNode raw : results) {
//      try {
//        resultset.add(raw);
//      } catch (RecordsetException ex) {
//        parseError++;
//        lastException = ex;
//        lastRecord = raw;
//      }
//    }
//
//    if (parseError > 0) {
//      logger.debug("Skipped {} records due to parser error", parseError);
//
//      if (lastException != null) {
//        logger.error("Last exception", lastException);
//      }
//
//      if (lastRecord != null) {
//        logger.error("Last raw record: {}", lastRecord);
//      }
//
//    }
//
//    return resultset;
//  }
//  public RecordSet fetchLastUpdate(Stream stream) throws RecordsetException, ConfigurationException, Storage.StorageException, Authentication.AuthenticationException {
//    ResultSet resultset = fetchData(stream, 1, 0);
//    return resultset.isEmpty() ? null : resultset.get(0);
//  }
    public void saveData(Stream stream, RecordSet record) throws ConfigurationException, Storage.StorageException, JsonProcessingException, IOException, Authentication.AuthenticationException {

        record.setStream(stream);
        record.userId = stream.getServiceObject().getUserId();

        getDataConnection().set(getDataId(stream, record), record.toJsonNode(), defaultDataTTL);
    }

    public void deleteData(Stream stream, RecordSet record) throws ConfigurationException, Storage.StorageException {
        getDataConnection().delete(getDataId(stream, record));
    }

//  public List<RecordSet> listData() throws ConfigurationException, Storage.StorageException, Authentication.AuthenticationException, IOException {
//
//    List<JsonNode> results = getDataConnection().list(
//            BaseQuery.queryBy("userId", auth.getUser().getUserId())
//    );
//
//    List<RecordSet> list = new ArrayList();
//    for (JsonNode raw : results) {
//      list.add(RecordSet.fromJSON(raw));
//    }
//
//    return list;
//  }
    public void deleteData(Stream stream) throws ConfigurationException, Storage.StorageException, Authentication.AuthenticationException, IOException, Indexer.IndexerException, RecordsetException {

        List<RecordSet> records = indexer.getStreamData(stream);

        for (RecordSet record : records) {
            deleteData(stream, record);
        }

    }

    public void deleteData(Collection<Stream> streams) throws ConfigurationException, Storage.StorageException, Authentication.AuthenticationException, IOException, Indexer.IndexerException, RecordsetException {

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

    public ActionStatus getActionStatus(Action action) throws IOException, ConfigurationException, Storage.StorageException {

        JsonNode rawStatus = getActionConnection().get(getActionId(action));

        ActionStatus actionStatus = ActionStatus.parseJSON(rawStatus);

        return actionStatus;
    }

    public ActionStatus saveActionStatus(Action action, String status) throws ConfigurationException, Storage.StorageException {

        ActionStatus actionStatus = new ActionStatus(action, status);

        getActionConnection().set(getActionId(action), actionStatus.toJsonNode(), defaultDataTTL);

        return actionStatus;
    }

    public void deleteActionStatus(Action action) throws ConfigurationException, Storage.StorageException {
        getActionConnection().delete(getActionId(action));
    }

    public void deleteActionStatus(Collection<Action> changedActions) throws ConfigurationException, Storage.StorageException {
        if (!changedActions.isEmpty()) {
            // drop action data
            for (Action changedAction : changedActions) {
                logger.debug("Removing action {} data for object {}", changedAction.name, changedAction.getServiceObject().id);
                deleteActionStatus(changedAction);
            }
        }
    }

}
