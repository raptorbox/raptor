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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.jvnet.hk2.annotations.Service;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.db.StorageProvider;
import org.createnet.raptor.db.query.BaseQuery;
import org.createnet.raptor.db.query.ListQuery;
import org.createnet.raptor.http.configuration.StorageConfiguration;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.createnet.raptor.models.data.ActionStatus;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.exception.RecordsetException;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.models.objects.serializer.ServiceObjectView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class StorageService {

  private final int defaultDataTTL = 3 * 30; // 3 months, 90 days
  private final Logger logger = LoggerFactory.getLogger(StorageService.class);

  @Inject
  ConfigurationService configuration;

  @Inject
  AuthService auth;

  private Storage storage;

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

  public Storage.Connection getObjectConnection() throws ConfigurationException, Storage.StorageException {
    return getStorage().getConnection(ConnectionId.objects.name());
  }

  public Storage.Connection getDataConnection() throws ConfigurationException, Storage.StorageException {
    return getStorage().getConnection(ConnectionId.data.name());
  }

  public Storage.Connection getActionConnection() throws ConfigurationException, Storage.StorageException {
    return getStorage().getConnection(ConnectionId.actuations.name());
  }

  public ServiceObject getObject(String id) throws Storage.StorageException, RaptorComponent.ParserException, ConfigurationException {
    String json = getObjectConnection().get(id);
    if (json == null) {
      return null;
    }
    ServiceObject obj = new ServiceObject();
    obj.parse(json);
    return obj;
  }

  public String saveObject(ServiceObject obj) throws ConfigurationException, Storage.StorageException, RaptorComponent.ParserException, RaptorComponent.ValidationException, Authentication.AuthenticationException {

    obj.validate();

    if (obj.id == null) {
      obj.id = ServiceObject.generateUUID();
    }

    obj.userId = auth.getUser().getUserId();

    String json = obj.toJSON(ServiceObjectView.Internal);
    getObjectConnection().set(obj.id, json, 0);
    return obj.id;
  }

  public void deleteObject(ServiceObject obj) throws ConfigurationException, Storage.StorageException, Authentication.AuthenticationException, IOException {
        
    // cleanup data
    deleteData(obj.streams.values());
    deleteActionStatus(obj.actions.values());
    
    getObjectConnection().delete(obj.id);    
    
  }

  public List<ServiceObject> listObjects() throws ConfigurationException, Storage.StorageException, Authentication.AuthenticationException, IOException {

    List<String> results = getObjectConnection().list(BaseQuery.queryBy("userId", auth.getUser().getUserId()));

    List<ServiceObject> list = new ArrayList();
    for (String raw : results) {
      list.add(ServiceObject.fromJSON(raw));
    }

    return list;
  }

  // Data 
  protected String getDataId(Stream stream, RecordSet record) {
    return stream.getServiceObject().id + "-" + stream.name + "-" + record.getLastUpdate().getTime();
  }

  public ResultSet fetchData(Stream stream) throws RecordsetException, ConfigurationException, Storage.StorageException, Authentication.AuthenticationException {
    return fetchData(stream, 0);
  }

  public ResultSet fetchData(Stream stream, int limit) throws RecordsetException, ConfigurationException, Storage.StorageException, Authentication.AuthenticationException {

    ResultSet resultset = new ResultSet(stream);

    BaseQuery query = BaseQuery.queryBy(
            "userId", auth.getUser().getUserId(),
            "streamId", stream.name
    );

    if (limit > 0) {
      query.limit = limit;
      query.setSort("lastUpdate", ListQuery.Sort.ASC);
    }

    List<String> results = getDataConnection().list(query);

    for (String raw : results) {
      resultset.add(raw);
    }

    return resultset;
  }

  public RecordSet fetchLastUpdate(Stream stream) throws RecordsetException, ConfigurationException, Storage.StorageException, Authentication.AuthenticationException {
    ResultSet resultset = fetchData(stream, 1);
    return resultset.data.isEmpty() ? null : resultset.data.get(0);
  }

  public void saveData(Stream stream, RecordSet record) throws ConfigurationException, Storage.StorageException, JsonProcessingException, IOException, Authentication.AuthenticationException {
    record.userId = auth.getUser().getUserId();
    record.streamId = stream.name;
    getDataConnection().set(getDataId(stream, record), record.toJson(), defaultDataTTL);
  }

  public void deleteData(Stream stream, RecordSet record) throws ConfigurationException, Storage.StorageException {
    getDataConnection().delete(getDataId(stream, record));
  }

  public List<RecordSet> listData() throws ConfigurationException, Storage.StorageException, Authentication.AuthenticationException, IOException {

    List<String> results = getDataConnection().list(
            BaseQuery.queryBy("userId", auth.getUser().getUserId())
    );

    List<RecordSet> list = new ArrayList();
    for (String raw : results) {
      list.add(RecordSet.fromJSON(raw));
    }

    return list;
  }

  // @TODO: introduce batch operation in storage
  public void deleteData(Stream stream) throws ConfigurationException, Storage.StorageException, Authentication.AuthenticationException, IOException {

    BaseQuery query = BaseQuery.queryBy("userId", auth.getUser().getUserId());

    query.getQueryOptions().timeout = 30;
    query.getQueryOptions().timeoutUnit = TimeUnit.SECONDS;

    List<String> results = getDataConnection().list(query);

    for (String raw : results) {
      RecordSet rs = RecordSet.fromJSON(raw);
      deleteData(stream, rs);
    }

  }

  public void deleteData(Collection<Stream> changedStreams) throws ConfigurationException, Storage.StorageException, Authentication.AuthenticationException, IOException {
    if (!changedStreams.isEmpty()) {
      //drop stream data
      for (Stream changedStream : changedStreams) {
        logger.debug("Removing stream {} data for object {}", changedStream.name, changedStream.getServiceObject().id);
        deleteData(changedStream);
      }
    }
  }

  // Actuations
  protected String getActionId(Action action) {
    return action.getServiceObject().id + "-" + action.name;
  }

  public ActionStatus getActionStatus(Action action) throws ConfigurationException, Storage.StorageException, IOException, RaptorComponent.ParserException {

    String rawStatus = getActionConnection().get(getActionId(action));

    ActionStatus actionStatus = ActionStatus.parseJSON(rawStatus);

    return actionStatus;
  }

  public ActionStatus saveActionStatus(Action action, String status) throws IOException, ConfigurationException, Storage.StorageException, RaptorComponent.ParserException {

    ActionStatus actionStatus = new ActionStatus(action, status);

    getActionConnection().set(getActionId(action), actionStatus.toJSON(ActionStatus.ViewType.Internal), defaultDataTTL);

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
