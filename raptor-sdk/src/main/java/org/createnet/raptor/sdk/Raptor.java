/*
 * Copyright 2017 Luca Capra <luca.capra@fbk.eu>.
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
package org.createnet.raptor.sdk;

import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.sdk.admin.AdminClient;
import org.createnet.raptor.sdk.config.Config;
import org.createnet.raptor.sdk.api.ActionClient;
import org.createnet.raptor.sdk.api.AuthClient;
import org.createnet.raptor.sdk.api.HttpClient;
import org.createnet.raptor.sdk.api.DeviceClient;
import org.createnet.raptor.sdk.api.StreamClient;
import org.createnet.raptor.sdk.events.MqttEventEmitter;
import org.createnet.raptor.models.objects.RaptorComponent;

/**
 * Raptor SDK wrapper entry point
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public class Raptor implements IClient, RaptorComponent {

    final protected Config config;
    final protected HttpClient client;
    final protected MqttEventEmitter emitter; 
    
    final public AuthClient Auth;
    final public StreamClient Stream;
    final public ActionClient Action;
    final public DeviceClient Device;
    
    final public AdminClient Admin;

    /**
     * Instantiate the client
     *
     * @param url
     * @param username
     * @param password
     */
    public Raptor(String url, String username, String password) {
        this(new Config(url, username, password));
    }

    /**
     * Instantiate the client
     *
     * @param url
     * @param token
     */
    public Raptor(String url, String token) {
        this(new Config(url, token));
    }

    /**
     * Instantiate the client
     *
     * @param url
     * @param token
     */
    public Raptor(String url, Token token) {
        this(new Config(url, token));
    }

    /**
     * Instantiate the client
     *
     * @param config
     */
    public Raptor(Config config) {

        this.config = config;
        client = new HttpClient(this);
        emitter = new MqttEventEmitter(this);

        Auth = new AuthClient(this);
        Stream = new StreamClient(this);
        Device = new DeviceClient(this);
        Action = new ActionClient(this);
        
        Admin = new AdminClient(this);

    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public Raptor getContainer() {
        return this;
    }

    @Override
    public HttpClient getClient() {
        return client;
    }

    public MqttEventEmitter getEmitter() {
        return emitter;
    }
        
}
