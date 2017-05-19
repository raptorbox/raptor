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
package org.createnet.raptor.api.common.dispatcher;

import org.createnet.raptor.config.ConfigurationLoader;
import org.createnet.raptor.dispatcher.Dispatcher;
import org.createnet.raptor.dispatcher.DispatcherConfiguration;
import org.createnet.raptor.models.acl.Permissions;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.DeviceContainer;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.models.payload.ActionPayload;
import org.createnet.raptor.models.payload.DataPayload;
import org.createnet.raptor.models.payload.DevicePayload;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.createnet.raptor.models.payload.StreamPayload;
import org.createnet.raptor.models.tree.TreeNode;
import org.createnet.raptor.sdk.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class DispatcherService implements InitializingBean, DisposableBean {

    final private Logger logger = LoggerFactory.getLogger(DispatcherService.class);

    private Dispatcher dispatcher;

    public Dispatcher getDispatcher() {
        if (dispatcher == null) {
            dispatcher = new Dispatcher();
            dispatcher.initialize(getConfiguration());
        }
        return dispatcher;
    }

    protected DispatcherConfiguration getConfiguration() {
        return (DispatcherConfiguration) ConfigurationLoader
                .getConfiguration("dispatcher", DispatcherConfiguration.class);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        getDispatcher();
    }

    @Override
    public void destroy() throws Exception {
        getDispatcher().close();
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

        return String.format(Topics.DEVICE, id);
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

        return String.format(Topics.USER, userId);
    }

    public void notifyEvent(String topic, DispatcherPayload message) {
        logger.debug("Notifying {} {}.{}", topic, message.getType(), message.getOp());
        getDispatcher().add(topic, message.toString());
    }

    protected void notifyTreeEvent(TreeNode n, DispatcherPayload payload) {
        String topic = String.format(Topics.GROUP, n.path());
        notifyEvent(topic, payload);
        
    }

    protected void notifyUserEvent(Permissions op, Device obj, DispatcherPayload payload) {
        String topic = getUserEventsTopic(obj);
        notifyEvent(topic, payload);
    }

    protected void notifyDeviceEvent(Permissions op, Device obj) {

        String topic = getEventsTopic(obj);
        DevicePayload payload = new DevicePayload(obj, op);

        notifyEvent(topic, payload);
        notifyUserEvent(op, obj, payload);
    }

    public void notifyDataEvent(Stream stream, RecordSet record) {

        String topic = getEventsTopic(stream);
        StreamPayload payload = new StreamPayload(stream, Permissions.data, record.toJsonNode());

        notifyEvent(topic, payload);
        notifyUserEvent(Permissions.push, stream.getDevice(), payload);
    }

    protected void notifyActionEvent(Permissions op, Action action, String status) {

        String topic = getEventsTopic(action);

        String data = null;
        if (status != null) {
            data = status;
        }

        ActionPayload payload = new ActionPayload(action, op, data);

        notifyEvent(topic, payload);
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

}
