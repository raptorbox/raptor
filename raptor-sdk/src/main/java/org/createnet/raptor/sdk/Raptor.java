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
import org.createnet.raptor.sdk.api.StreamClient;
import org.createnet.raptor.sdk.events.MqttEventEmitter;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.sdk.admin.ProfileClient;
import org.createnet.raptor.sdk.api.AppClient;
import org.createnet.raptor.sdk.api.InventoryClient;
import org.createnet.raptor.sdk.api.TreeClient;

/**
 * Raptor SDK wrapper entry point
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public class Raptor implements IClient, RaptorComponent {

    final protected Config config;

    protected HttpClient client;
    protected MqttEventEmitter emitter;

    protected AdminClient Admin;
    protected AuthClient Auth;
    protected StreamClient Stream;
    protected ActionClient Action;
    protected InventoryClient Inventory;
    protected TreeClient Tree;
    protected AppClient App;

    public AdminClient Admin() {
        if (Admin == null) {
            Admin = new AdminClient(getContainer());
        }
        return Admin;
    }

    public AuthClient Auth() {
        if (Auth == null) {
            Auth = new AuthClient(getContainer());
        }
        return Auth;
    }

    public StreamClient Stream() {
        if (Stream == null) {
            Stream = new StreamClient(getContainer());
        }
        return Stream;
    }

    public ActionClient Action() {
        if (Action == null) {
            Action = new ActionClient(getContainer());
        }
        return Action;
    }

    public InventoryClient Inventory() {
        if (Inventory == null) {
            Inventory = new InventoryClient(getContainer());
        }
        return Inventory;
    }

    public TreeClient Tree() {
        if (Tree == null) {
            Tree = new TreeClient(getContainer());
        }
        return Tree;
    }
    
    public AppClient App() {
        if (App == null) {
            App = new AppClient(getContainer());
        }
        return App;
    }

    public ProfileClient Profile() {
        return Admin().User().Profile();
    }
    
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
     * @param url
     */
    public Raptor(String url) {
        this(new Config(url));
    }

    /**
     * Instantiate the client
     *
     * @param config
     */
    public Raptor(Config config) {
        this.config = config;
    }

    public Raptor setToken(String token) {
        this.getConfig().setToken(token);
        return this;
    }

    public Raptor setCredentials(String username, String password) {
        this.getConfig().setCredentials(username, password);
        return this;
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
        if (client == null) {
            client = new HttpClient(this);
        }
        return client;
    }

    @Override
    public MqttEventEmitter getEmitter() {
        if (emitter == null) {
            emitter = new MqttEventEmitter(this);
        }
        return emitter;
    }

}
