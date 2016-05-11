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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.jvnet.hk2.annotations.Service;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.db.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class StorageService {
  
  private final Logger logger = LoggerFactory.getLogger(StorageService.class);
  
  @Inject
  ConfigurationService configuration;
  
  private Storage storage;

  private enum ConnectionId {
    objects, data, subscriptions, actuations
  }

  protected Storage getStorage() throws IOException, Storage.StorageException {

    if (storage == null) {
      storage = new StorageProvider();
      storage.initialize(configuration.getStorage());
      storage.setup(false);
      storage.connect();
    }

    return storage;
  }

  protected Storage.Connection getObjectConnection() throws IOException, Storage.StorageException {
    return getStorage().getConnection(ConnectionId.objects.name());
  }

  public ServiceObject getObject(String id) throws Storage.StorageException, RaptorComponent.ParserException, IOException {
    String json = getObjectConnection().get(id);
    if (json == null) {
      return null;
    }
    ServiceObject obj = new ServiceObject();
    obj.parse(json);
    return obj;
  }

  public List<ServiceObject> listObjects(String userId) throws Storage.StorageException, RaptorComponent.ParserException, IOException {
    return new ArrayList();
  }

  public String saveObject(ServiceObject obj) throws IOException, Storage.StorageException, RaptorComponent.ParserException, RaptorComponent.ValidationException {
    obj.validate();
    obj.id = ServiceObject.generateUUID();
    getObjectConnection().set(obj.id, obj.toJSON(), 0);    
    return obj.id;
  }

  public void deleteObject(String id) throws IOException, Storage.StorageException {
    getObjectConnection().delete(id);
  }  
  
}
