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

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import javax.inject.Inject;
import org.createnet.raptor.auth.authentication.Authentication;
import org.jvnet.hk2.annotations.Service;
import org.createnet.raptor.dispatcher.Dispatcher;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.createnet.raptor.events.Emitter;
import org.createnet.raptor.events.Event;
import org.createnet.raptor.http.events.ActionEvent;
import org.createnet.raptor.http.events.DataEvent;
import org.createnet.raptor.http.events.ObjectEvent;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class DispatcherService implements RaptorService {

  private final Logger logger = LoggerFactory.getLogger(DispatcherService.class);

  @Inject
  ConfigurationService configuration;

  @Inject
  AuthService auth;

  @Inject
  EventEmitterService emitter;

  Emitter.Callback emitterCallback = new Emitter.Callback() {
    @Override
    public void run(Event event) throws Emitter.EmitterException {

      try {

        logger.debug("Processing dispatcher event {} (parent: {})", event.getEvent(), event.getParentEvent());

        switch (event.getParentEvent()) {
          case "create":
          case "update":
          case "delete":
            ObjectEvent objEvent = (ObjectEvent) event;

            if (!objEvent.getObject().settings.eventsEnabled()) {
              return;
            }

            notifyObjectEvent(objEvent.getEvent(), objEvent.getObject());
            break;
          case "push":
            // notify data event
            DataEvent dataEvent = (DataEvent) event;

            if (!dataEvent.getStream().getServiceObject().settings.eventsEnabled()) {
              return;
            }

            notifyDataEvent(dataEvent.getStream(), dataEvent.getRecord());
            break;
          case "execute":
          case "deleteAction":
            // notify event
            ActionEvent actionEvent = (ActionEvent) event;

            if (!actionEvent.getAction().getServiceObject().settings.eventsEnabled()) {
              return;
            }

            String op = event.getEvent().equals("execute") ? "execute" : "delete";
            notifyActionEvent(op, actionEvent.getAction(), actionEvent.getActionStatus().status);
            break;
        }

      } catch (ConfigurationException | RaptorComponent.ParserException | Authentication.AuthenticationException | IOException e) {
        logger.error("Failed to dispatch message", e);
      }

    }
  };
   
    
  @PostConstruct
  @Override
  public void initialize() throws ServiceException {
    try {
      getDispatcher();
    } catch (Exception e) {
      throw new ServiceException(e);
    }
  }

  @PreDestroy
  @Override
  public void shutdown() throws ServiceException {
    try {
      removeEmitterCallback();
      getDispatcher().close();
      dispatcher = null;
    } catch (Exception e) {
      throw new ServiceException(e);
    }
  }

  public enum MessageType {
    object, stream, actuation
  }

  public enum ObjectOperation {
    create, update, delete, push
  }

  public enum ActionOperation {
    execute, delete
  }

  private Dispatcher dispatcher;

  public DispatcherService() {
  }

  public Dispatcher getDispatcher() throws ConfigurationException {
    if (dispatcher == null) {
      dispatcher = new Dispatcher();
      dispatcher.initialize(configuration.getDispatcher());
      addEmitterCallback();
    }
    return dispatcher;
  }

  protected ObjectNode createObjectMessage(ServiceObject obj) throws ConfigurationException, Authentication.AuthenticationException {

    ObjectNode message = ServiceObject.getMapper().createObjectNode();

    message.put("userId", auth.getUser().getUserId());
    message.set("object", obj.toJsonNode());

    return message;
  }

  public void notifyObjectEvent(String op, ServiceObject obj) throws ConfigurationException, RaptorComponent.ParserException, Authentication.AuthenticationException {

    String topic = obj.id + "/events";

    ObjectNode message = createObjectMessage(obj);

    message.put("type", MessageType.object.toString());
    message.put("op", op);

    getDispatcher().add(topic, message.toString());
  }

  public void notifyDataEvent(Stream stream, RecordSet record) throws IOException, ConfigurationException {

    String topic = stream.getServiceObject().id + "/events";

    ObjectNode message = ServiceObject.getMapper().createObjectNode();

    message.put("userId", stream.getServiceObject().getUserId());
    message.set("object", stream.getServiceObject().toJsonNode());
    message.put("type", MessageType.stream.toString());
    message.put("op", "data");
    message.put("streamId", stream.name);
    message.set("data", record.toJsonNode());

    getDispatcher().add(topic, message.toString());
  }

  public void notifyActionEvent(String op, Action action, String status) throws IOException, ConfigurationException, Authentication.AuthenticationException {

    String topic = action.getServiceObject().id + "/events";

    ObjectNode message = createObjectMessage(action.getServiceObject());

    message.put("type", MessageType.actuation.toString());
    message.put("op", op);

    message.put("actionId", action.name);

    if (status != null) {
      message.put("data", status);
    }

    getDispatcher().add(topic, message.toString());
  }

  public void pushData(Stream stream, RecordSet records) throws ConfigurationException, Authentication.AuthenticationException, IOException {
    String topic = stream.getServiceObject().id + "/stream/" + stream.name + "/updates";
    getDispatcher().add(topic, records.toJson());
  }

  public void actionTrigger(Action action, String status) throws ConfigurationException, Authentication.AuthenticationException {
    String topic = action.getServiceObject().id + "/actuations/" + action.name;
    getDispatcher().add(topic, status);
  }

  private void addEmitterCallback() {
    logger.debug("Register dispatcher event trigger");
    emitter.on(EventEmitterService.EventName.all, emitterCallback);
  }

  private void removeEmitterCallback() {
    logger.debug("Unregister dispatcher event trigger");
    emitter.off(EventEmitterService.EventName.all, emitterCallback);
  }

}
