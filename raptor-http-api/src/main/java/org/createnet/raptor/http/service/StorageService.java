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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.jvnet.hk2.annotations.Service;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.db.StorageProvider;
import org.createnet.raptor.http.configuration.StorageConfiguration;
import org.createnet.raptor.http.exception.ConfigurationException;
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

  protected Storage.Connection getObjectConnection() throws ConfigurationException, Storage.StorageException {
    return getStorage().getConnection(ConnectionId.objects.name());
  }
  
  protected Storage.Connection getDataConnection() throws ConfigurationException, Storage.StorageException {
    return getStorage().getConnection(ConnectionId.data.name());
  }
  
  protected Storage.Connection getActionConnection() throws ConfigurationException, Storage.StorageException {
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
    
    if(obj.id == null) {
      obj.id = ServiceObject.generateUUID();
    }

    obj.userId = auth.getUser().getUserId();
    
    String json = obj.toJSON(ServiceObjectView.Internal);
    getObjectConnection().set(obj.id, json, 0);
    return obj.id;
  }

  public void deleteObject(String id) throws ConfigurationException, Storage.StorageException {
    getObjectConnection().delete(id);
  }
  
  public List<ServiceObject> listObjects() throws ConfigurationException, Storage.StorageException, Authentication.AuthenticationException, IOException {
    
    List<String> results = getObjectConnection().list("userId", auth.getUser().getUserId());
    
    List<ServiceObject> list = new ArrayList();
    for(String raw : results) {
      list.add(ServiceObject.fromJSON(raw));
    }
    
    return list;
  }

  // Data 
  
  protected String getDataId(Stream stream, RecordSet record) {
    return stream.getServiceObject().id + "-" + stream.name + "-" + record.getLastUpdate().getTime();
  }
  
  public ResultSet fetchData(Stream stream) throws RecordsetException, ConfigurationException, Storage.StorageException, Authentication.AuthenticationException {
    
    ResultSet resultset = new ResultSet(stream);

    List<String> results = getDataConnection().list("userId", auth.getUser().getUserId());
    for(String raw : results) {
      resultset.add(raw);
    }
    
    return resultset;
  }
  
  public void saveData(Stream stream, RecordSet record) throws ConfigurationException, Storage.StorageException, JsonProcessingException, IOException, Authentication.AuthenticationException {
    record.userId = auth.getUser().getUserId();
    record.streamId = stream.name;
    getDataConnection().set(getDataId(stream, record), record.toJson(), defaultDataTTL);
  }
  
  public void deleteData(Stream stream, RecordSet record) throws ConfigurationException, Storage.StorageException {
    getDataConnection().delete(getDataId(stream, record));
  }
  
  public List<RecordSet> listData() throws ConfigurationException, Storage.StorageException, Authentication.AuthenticationException, IOException  {
    
    List<String> results = getDataConnection().list("userId", auth.getUser().getUserId());
    
    List<RecordSet> list = new ArrayList();
    for(String raw : results) {
      list.add(RecordSet.fromJSON(raw));
    }
    
    return list;
  }
  
  // Actuations
  
  protected String getActionId(Action action) {
    return action.getServiceObject().id + "-" + action.name;
  }
  
  public String getActionStatus(Action action) throws ConfigurationException, Storage.StorageException {
    return getActionConnection().get(getActionId(action));
  }
  
  public String saveActionStatus(Action action, String status) throws IOException, ConfigurationException, Storage.StorageException{

    ObjectNode json = ServiceObject.getMapper().createObjectNode();
    
    json.put("id", ServiceObject.generateUUID());
    json.put("status", status);
    json.put("actionId", action.name);
    json.put("objectId", action.getServiceObject().id);
    
    getActionConnection().set(getActionId(action), json.toString(), defaultDataTTL);
    
    return json.get("id").asText();
  }
  
  public void deleteActionStatus(Action action) throws ConfigurationException, Storage.StorageException {
    getActionConnection().delete(getActionId(action));
  }

}
