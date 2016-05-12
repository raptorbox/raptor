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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.inject.Inject;
import org.createnet.raptor.auth.authentication.Authentication;
import org.jvnet.hk2.annotations.Service;
import org.createnet.raptor.dispatcher.Dispatcher;
import org.createnet.raptor.http.exception.ConfigurationException;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.serializer.ServiceObjectView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class DispatcherService {
  
  final JsonNodeFactory jsonFactory = JsonNodeFactory.instance;
  private final Logger logger = LoggerFactory.getLogger(DispatcherService.class);
  
  @Inject
  ConfigurationService configuration;
  
  @Inject
  AuthService auth;
  
  public enum ObjectOperation {
    create, update, delete
  }
  
  private Dispatcher dispatcher;
  
  public Dispatcher getDispatcher() throws ConfigurationException {
    if(dispatcher == null) {
      dispatcher = new Dispatcher();
      dispatcher.initialize(configuration.getDispatcher());
    }
    return dispatcher;
  }
  
  public void notifyObjectEvent(ObjectOperation op, ServiceObject obj) throws ConfigurationException, RaptorComponent.ParserException, Authentication.AutenticationException {
    
    String topic = "events";
    
    ObjectNode message = jsonFactory.objectNode();
    
    message.put("op", op.name());
    message.put("userId", auth.getUser().getUserId());
    
    message.set("object", obj.toJsonNode());
    
    getDispatcher().add(topic, message.toString());
  }
  
}
