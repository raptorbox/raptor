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
package org.createnet.raptor.http.api;

import javax.inject.Inject;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.createnet.raptor.http.service.AuthService;
import org.createnet.raptor.http.service.DispatcherService;
import org.createnet.raptor.http.service.EventEmitterService;
import org.createnet.raptor.http.service.IndexerService;
import org.createnet.raptor.http.service.StorageService;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.Stream;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
abstract public class AbstractApi {
  

  @Inject
  protected EventEmitterService emitter;

  @Inject
  protected StorageService storage;

  @Inject
  protected IndexerService indexer;

  @Inject
  protected DispatcherService dispatcher;

  @Inject
  protected AuthService auth;
  
  protected ServiceObject loadObject(String id) throws Authorization.AuthorizationException, Storage.StorageException, RaptorComponent.ParserException, ConfigurationException {

    ServiceObject obj = storage.getObject(id);

    if (!auth.isAllowed(id, Authorization.Permission.Read)) {
      throw new NotAllowedException("Cannot access object");
    }
    
    if (obj == null) {
      throw new NotFoundException("Object "+ id +" not found");
    }    

    return obj;
  }
  
  protected Stream loadStream(String streamId, ServiceObject obj){
    
    Stream stream = obj.streams.getOrDefault(streamId, null);
    
    if(stream == null) {
      throw new NotFoundException("Stream "+ streamId + "not found");
    }
    
    return stream;
  }

  protected Action loadAction(String actionId, ServiceObject obj){
    
    Action action = obj.actions.getOrDefault(actionId, null);
    
    if(action == null) {
      throw new NotFoundException("Action "+ actionId + "not found");
    }
    
    return action;
  }
  
  
}
