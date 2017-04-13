/*
 * Copyright 2017 FBK/CREATE-NET
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
package org.createnet.raptor.service.tools;

import org.createnet.raptor.service.AbstractRaptorService;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import org.createnet.raptor.dispatcher.Dispatcher;
import org.createnet.raptor.models.payload.ActionPayload;
import org.createnet.raptor.models.payload.DataPayload;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.createnet.raptor.models.payload.ObjectPayload;
import org.createnet.raptor.models.payload.StreamPayload;
import org.createnet.raptor.events.Emitter;
import org.createnet.raptor.events.Event;
import org.createnet.raptor.events.Event.EventName;
import org.createnet.raptor.events.type.ActionEvent;
import org.createnet.raptor.events.type.DataEvent;
import org.createnet.raptor.events.type.ObjectEvent;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.DeviceContainer;
import org.createnet.raptor.models.objects.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
@Singleton
public class DispatcherService extends AbstractRaptorService {

    private final static Logger logger = LoggerFactory.getLogger(DispatcherService.class);

    @Inject
    ConfigurationService configuration;

    @Inject
    EventEmitterService emitter;

    Emitter.Callback emitterCallback = new Emitter.Callback() {
        @Override
        public void run(Event event) {

            logger.debug("Processing dispatcher event {} (parent: {})", event.getEvent(), event.getParentEvent());

            switch (event.getParentEvent()) {
                case "create":
                case "update":
                case "delete":
                    ObjectEvent objEvent = (ObjectEvent) event;

                    if (!objEvent.getObject().settings.eventsEnabled()) {
                        return;
                    }

                    notifyObjectEvent(objEvent.getParentEvent(), objEvent.getObject());
                    break;
                case "push":

                    // notify data event
                    DataEvent dataEvent = (DataEvent) event;

                    if (!dataEvent.getStream().getDevice().settings.eventsEnabled()) {
                        return;
                    }

                    // send update on the data topic
                    pushData(dataEvent.getStream(), dataEvent.getRecord());

                    //notify data event
                    notifyDataEvent(dataEvent.getStream(), dataEvent.getRecord());
                    break;
                case "execute":
                case "deleteAction":
                    // notify event
                    ActionEvent actionEvent = (ActionEvent) event;

                    if (!actionEvent.getAction().getDevice().settings.eventsEnabled()) {
                        return;
                    }

                    // invoke action over mqtt
                    actionTrigger(actionEvent.getAction(), actionEvent.getActionStatus().status);

                    // notify of action event
                    String op = event.getEvent().equals("execute") ? "execute" : "delete";
                    notifyActionEvent(op, actionEvent.getAction(), actionEvent.getActionStatus().status);
                    break;
            }

        }
    };

    @PostConstruct
    @Override
    public void initialize() {
        try {
            logger.debug("Initializing dispatcher");
            addEmitterCallback();
            getDispatcher();
        } catch (Exception e) {
            logger.debug("Failed dispatcher initialization");
            throw new ServiceException(e);
        }
    }

    @PreDestroy
    @Override
    public void shutdown() {
        try {
            removeEmitterCallback();
            getDispatcher().close();
            dispatcher = null;
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    private Dispatcher dispatcher;

    public DispatcherService() {
    }

    public Dispatcher getDispatcher() {
        if (dispatcher == null) {
            dispatcher = new Dispatcher();
            dispatcher.initialize(configuration.getDispatcher());
        }
        return dispatcher;
    }

    protected String getEventsTopic(DeviceContainer c) {

        Device obj = c.getDevice();
        if (obj == null) {
            throw new RaptorComponent.ParserException("Device is null");
        }

        String id = obj.getId();
        if (id == null) {
            throw new RaptorComponent.ParserException("Device.id is null");
        }

        return id + "/events";
    }
    
    protected String getUserEventsTopic(DeviceContainer c) {

        Device obj = c.getDevice();
        if (obj == null) {
            throw new RaptorComponent.ParserException("Device is null");
        }

        String userId = obj.getUserId();
        if (userId == null) {
            throw new RaptorComponent.ParserException("Device.userId is null");
        }

        return userId + "/events";
    }

    public void notifyEvent(String topic, DispatcherPayload message) {
        logger.debug("Notifying {} {}.{}", topic, message.getType(), message.getOp());
        getDispatcher().add(topic, message.toString());
    }

    protected void notifyTreeEvent(DeviceContainer c, DispatcherPayload payload) {

        Device obj = c.getDevice();
        if (obj == null) {
            throw new RaptorComponent.ParserException("Device is null");
        }

        String path = obj.path();
        if (path == null) {
            logger.debug("Object {} (pid:{}) path is empty", obj.id, obj.parentId);
            return;
        }

        notifyEvent(path + "/events", payload);
    }

    protected void notifyUserEvent(String op, Device obj, DispatcherPayload payload) {
        String topic = getUserEventsTopic(obj);
        notifyEvent(topic, payload);
    }

    protected void notifyObjectEvent(String op, Device obj) {

        String topic = getEventsTopic(obj);
        ObjectPayload payload = new ObjectPayload(obj, op);

        notifyEvent(topic, payload);
        notifyTreeEvent(obj, payload);
        notifyUserEvent(op, obj, payload);
    }

    public void notifyDataEvent(Stream stream, RecordSet record) {

        String topic = getEventsTopic(stream);
        StreamPayload payload = new StreamPayload(stream, "data", record.toJsonNode());

        notifyEvent(topic, payload);
        notifyTreeEvent(stream, payload);
        notifyUserEvent("push", stream.getDevice(), payload);
    }

    protected void notifyActionEvent(String op, Action action, String status) {

        String topic = getEventsTopic(action);

        String data = null;
        if (status != null) {
            data = status;
        }

        ActionPayload payload = new ActionPayload(action, op, data);

        notifyEvent(topic, payload);
        notifyTreeEvent(action, payload);
        notifyUserEvent(op, action.getDevice(), payload);
    }

    public void pushData(Stream stream, RecordSet records) {
        String topic = stream.getDevice().id + "/streams/" + stream.name + "/updates";
        notifyEvent(topic, new DataPayload(records.toJson()));
    }

    public void actionTrigger(Action action, String status) {
        String topic = action.getDevice().id + "/actions/" + action.name;
        notifyEvent(topic, new DataPayload(status));
    }

    private void addEmitterCallback() {
        logger.debug("Register dispatcher event trigger");
        emitter.on(EventName.all, emitterCallback);
    }

    private void removeEmitterCallback() {
        logger.debug("Unregister dispatcher event trigger");
        emitter.off(EventName.all, emitterCallback);
    }

}
