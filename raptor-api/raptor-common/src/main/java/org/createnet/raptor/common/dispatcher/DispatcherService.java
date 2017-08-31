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
package org.createnet.raptor.common.dispatcher;

import org.createnet.raptor.models.acl.Permissions;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.configuration.AuthConfiguration;
import org.createnet.raptor.models.configuration.DispatcherConfiguration;
import org.createnet.raptor.models.configuration.RaptorConfiguration;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.DeviceContainer;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.models.payload.ActionPayload;
import org.createnet.raptor.models.payload.DevicePayload;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.createnet.raptor.models.payload.StreamPayload;
import org.createnet.raptor.models.payload.TokenPayload;
import org.createnet.raptor.models.payload.UserPayload;
import org.createnet.raptor.models.tree.TreeNode;
import org.createnet.raptor.sdk.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class DispatcherService implements InitializingBean, DisposableBean {

    final private Logger logger = LoggerFactory.getLogger(DispatcherService.class);
    private DispatcherEngine dispatcher;
    
    @Autowired
    RaptorConfiguration config;

    public DispatcherEngine getDispatcher() {
        if (dispatcher == null) {
            dispatcher = new DispatcherEngine();
            
            AuthConfiguration.AdminUser user = config.getAuth().getServiceUser();

            if(user == null) {
                throw new RuntimeException("Service user not found, review your raptor.yml configuration");
            }            
            
            getConfiguration().setUsername(user.getUsername());
            getConfiguration().setPassword(user.getPassword());
            
            logger.debug("Connecting as MQTT user {}", getConfiguration().getUsername());
            dispatcher.initialize(getConfiguration());
        }
        return dispatcher;
    }

    /**
     * @todo rewrite this to use dispatcher
     * @return 
     */
    protected DispatcherConfiguration getConfiguration() {
        return config.getDispatcher();
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

        String id = obj.id();
        if (id == null) {
            throw new RaptorComponent.ParserException("Device.id is null");
        }

        return String.format(Topics.DEVICE, id);
    }

    protected String getStreamTopic(Stream s) {

        Device obj = s.getDevice();
        if (obj == null) {
            throw new RaptorComponent.ParserException("Device is null");
        }

        String id = obj.id();
        if (id == null) {
            throw new RaptorComponent.ParserException("Device.id is null");
        }

        return String.format(Topics.STREAM, id, s.name());
    }

    protected String getActionTopic(Action a) {

        Device obj = a.getDevice();
        if (obj == null) {
            throw new RaptorComponent.ParserException("Device is null");
        }

        String id = obj.id();
        if (id == null) {
            throw new RaptorComponent.ParserException("Device.id is null");
        }

        return String.format(Topics.ACTION, id, a.name());
    }

    protected String getUserEventsTopic(DeviceContainer c) {

        Device obj = c.getDevice();
        if (obj == null) {
            throw new RaptorComponent.ParserException("Device is null");
        }

        String userId = obj.userId();
        if (userId == null) {
            throw new RaptorComponent.ParserException("Device.userId is null");
        }

        return String.format(Topics.USER, userId);
    }
    
    protected String getUserEventsTopic(User u) {
        return String.format(Topics.USER, u.getUuid());
    }
    
    protected String getTokenEventsTopic(Token t) {
        return String.format(Topics.TOKEN, t.getId().toString());
    }

    /**
     *
     * @param topic
     * @param message
     */
    protected void notifyEvent(String topic, DispatcherPayload message) {
        
        logger.debug("Notifying {} {}.{}", topic, message.getType(), message.getOp());
        
        getDispatcher().add(topic, message.toString());

    }

    /**
     *
     * @param node
     * @param payload
     */
    public void notifyTreeEvent(TreeNode node, DispatcherPayload payload) {
        
        String topic = String.format(Topics.TREE, node.getId());
        
        notifyEvent(topic, payload);
        
    }

    /**
     *
     * @param op
     * @param obj
     * @param payload
     */
    protected void notifyUserEvent(Permissions op, Device obj, DispatcherPayload payload) {
        
        String topic = getUserEventsTopic(obj);
        
        notifyEvent(topic, payload);

    }

    /**
     *
     * @param op
     * @param obj
     */
    public void notifyDeviceEvent(Permissions op, Device obj) {

        String topic = getEventsTopic(obj);
        DevicePayload payload = new DevicePayload(obj, op);

        notifyEvent(topic, payload);
    }


    /**
     *
     * @param op
     * @param user
     */
    public void notifyUserEvent(Permissions op, User user) {
        String topic = getUserEventsTopic(user);
        UserPayload payload = new UserPayload(user, op);
        notifyEvent(topic, payload);
    }
    
    /**
     *
     * @param op
     * @param token
     */
    public void notifyTokenEvent(Permissions op, Token token) {
        String topic = getTokenEventsTopic(token);
        TokenPayload payload = new TokenPayload(token, op);
        notifyEvent(topic, payload);
    }

    /**
     *
     * @param stream
     * @param record
     */
    public void notifyDataEvent(Stream stream, RecordSet record) {
        
        StreamPayload payload = new StreamPayload(stream, Permissions.data, record);
        
        notifyEvent(getStreamTopic(stream), payload);
        notifyEvent(getEventsTopic(stream), payload);
        
    }

    /**
     *
     * @param op
     * @param action
     * @param status
     */
    public void notifyActionEvent(Permissions op, Action action, String status) {

        String data = null;
        if (status != null) {
            data = status;
        }

        ActionPayload payload = new ActionPayload(action, op, data);

        notifyEvent(getActionTopic(action), payload);        
        notifyEvent(getEventsTopic(action), payload);
    }
    
}
