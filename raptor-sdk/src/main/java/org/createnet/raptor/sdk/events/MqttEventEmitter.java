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
package org.createnet.raptor.sdk.events;

import org.createnet.raptor.sdk.events.callback.DeviceEventCallback;
import org.createnet.raptor.sdk.events.callback.ActionEventCallback;
import org.createnet.raptor.sdk.events.callback.StreamEventCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.createnet.raptor.models.app.App;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.sdk.AbstractClient;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.createnet.raptor.models.tree.TreeNode;
import org.createnet.raptor.sdk.Topics;
import org.createnet.raptor.sdk.events.callback.AppEventCallback;
import org.createnet.raptor.sdk.events.callback.TokenEventCallback;
import org.createnet.raptor.sdk.events.callback.TreeNodeEventCallback;
import org.createnet.raptor.sdk.events.callback.UserEventCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class MqttEventEmitter extends AbstractClient {

    final private Logger logger = LoggerFactory.getLogger(MqttEventEmitter.class);

    final private Map<String, List<EventCallback>> subscriptions = new ConcurrentHashMap();

    final private MqttClientHandler mqttHandler;
    private MessageEventListener mqttEventListener;

    public MqttEventEmitter(Raptor container) {
        super(container);
        mqttHandler = new MqttClientHandler(container);
    }

    /**
     * Return the MQTT client handler
     *
     * @return
     */
    public MqttClientHandler getMqttClientHandler() {
        return mqttHandler;
    }

    public interface EventCallback {
        public void trigger(DispatcherPayload payload);
    }

    protected String getDeviceTopic(Device obj) {
        return String.format(Topics.DEVICE, obj.getId());
    }
    
    protected String getAppTopic(App obj) {
        return String.format(Topics.APP, obj.getId());
    }

    protected String getUserTopic(User obj) {
        return String.format(Topics.USER, obj.getUuid());
    }

    protected String getTokenTopic(Token obj) {
        return String.format(Topics.TOKEN, obj.getId().toString());
    }

    protected String getGroupTopic(TreeNode n) {
        return String.format(Topics.TREE, n.getId(), "+");
    }

    protected String getStreamTopic(Stream stream) {
        String path = String.format(Topics.STREAM, stream.getDevice().getId(), stream.name());
        return path;
    }

    protected String getActionTopic(Action action) {
        String path = String.format(Topics.ACTION, action.getDevice().getId(), action.name());
        return path;
    }

    /**
     * Subscribe for action events
     *
     * @param action
     * @param ev
     */
    public void subscribe(Action action, ActionEventCallback ev) {

        registerCallback();

        String topic = getActionTopic(action);
        getMqttClientHandler().subscribe(topic);

        addTopicCallback(topic, ev);
    }

    /**
     * Subscribe for stream events
     *
     * @param stream
     * @param ev
     */
    public void subscribe(Stream stream, StreamEventCallback ev) {

        registerCallback();

        String topic = getStreamTopic(stream);
        getMqttClientHandler().subscribe(topic);

        addTopicCallback(topic, ev);
    }

    /**
     * Subscribe for tree node events
     *
     * @param node
     * @param ev
     */
    public void subscribe(TreeNode node, TreeNodeEventCallback ev) {

        registerCallback();

        String topic = getGroupTopic(node);
        getMqttClientHandler().subscribe(topic);

        addTopicCallback(topic, ev);
    }

    /**
     * Subscribe for device events
     *
     * @param app the app to listen for
     * @param ev
     */
    public void subscribe(App app, AppEventCallback ev) {

        registerCallback();

        String topic = getAppTopic(app);
        getMqttClientHandler().subscribe(topic);

        addTopicCallback(topic, ev);

    }

    /**
     * Subscribe for device events
     *
     * @param dev the device to listen for
     * @param ev
     */
    public void subscribe(Device dev, DeviceEventCallback ev) {

        registerCallback();

        String topic = getDeviceTopic(dev);
        getMqttClientHandler().subscribe(topic);

        addTopicCallback(topic, ev);

    }

    /**
     * Subscribe for user events
     *
     * @param user the user to listen for
     * @param ev
     */
    public void subscribe(User user, UserEventCallback ev) {

        registerCallback();

        String topic = getUserTopic(user);
        getMqttClientHandler().subscribe(topic);

        addTopicCallback(topic, ev);

    }

    /**
     * Subscribe for token events
     *
     * @param token the token to listen for
     * @param ev
     */
    public void subscribe(Token token, TokenEventCallback ev) {

        registerCallback();

        String topic = getTokenTopic(token);
        getMqttClientHandler().subscribe(topic);

        addTopicCallback(topic, ev);

    }

    protected void addTopicCallback(String key, EventCallback ev) {
        if (!this.subscriptions.containsKey(key)) {
            this.subscriptions.put(key, new ArrayList());
        }
        this.subscriptions.get(key).add(ev);
    }

    protected void removeTopicCallback(String key, EventCallback ev) {
        if (!this.subscriptions.containsKey(key)) {
            return;
        }
        if (this.subscriptions.get(key).contains(ev)) {
            this.subscriptions.get(key).remove(ev);
        }
    }

    protected void clearTopicCallback(String key) {
        if (!this.subscriptions.containsKey(key)) {
            return;
        }
        this.subscriptions.get(key).clear();
    }

    protected void registerCallback() {
        if (mqttEventListener == null) {
            mqttEventListener = new MessageEventListener() {
                @Override
                public void onMessage(MessageEventListener.Message message) {
                    DispatcherPayload payload = DispatcherPayload.parseJSON(message.content);
                    if (subscriptions.containsKey(message.topic)) {
                        subscriptions.get(message.topic).parallelStream().forEach(ev -> {
                            ev.trigger(payload);
                        });
                    }
                }
            };
            getMqttClientHandler().setCallback(mqttEventListener);
        }
    }

    /**
     * Unsubscribe a Stream for data updates
     *
     * @param obj obj from which to unsubscribe events
     */
    public void unsubscribe(Device obj) {

        String topic = getDeviceTopic(obj);

        getMqttClientHandler().unsubscribe(topic);
        clearTopicCallback(topic);

        // todo: ensure subscriptions is empty then clear global listener
//        mqttEventListener = null;
//        getMqttClientHandler().setCallback(null);
    }

}
