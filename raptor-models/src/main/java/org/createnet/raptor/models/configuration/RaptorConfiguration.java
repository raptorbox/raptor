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
package org.createnet.raptor.models.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RaptorConfiguration {

    private String url;
    private Boolean web;

    private DispatcherConfiguration dispatcher = new DispatcherConfiguration();
    private AuthConfiguration auth = new AuthConfiguration();
    private BrokerConfiguration broker = new BrokerConfiguration();
    
    public String getUrl() {
        return url;
    }

    public BrokerConfiguration getBroker() {
        return broker;
    }

    public DispatcherConfiguration getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(DispatcherConfiguration dispatcher) {
        this.dispatcher = dispatcher;
    }

    public AuthConfiguration getAuth() {
        return auth;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setAuth(AuthConfiguration auth) {
        this.auth = auth;
    }

    public void setBroker(BrokerConfiguration broker) {
        this.broker = broker;
    }

    public Boolean getWeb() {
        return web;
    }

    public void setWeb(Boolean web) {
        this.web = web;
    }
    
}
