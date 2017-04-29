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
package org.createnet.raptor.sdk.config;

import org.createnet.raptor.models.auth.Token;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class Config {

    final protected String url;

    protected String username;
    protected String password;
    protected String token;

    public Config(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.token = null;
    }

    public Config(String url, String token) {
        this.url = url;
        this.token = token;
        this.username = null;
        this.password = null;
    }

    public Config(String url, Token token) {
        this.url = url;
        this.token = token.getToken();
        this.username = null;
        this.password = null;
    }

    public Config(String url) {
        this.url = url;
    }

    public boolean hasCredentials() {
        return getToken() == null;
    }

    public boolean hasToken() {
        return getToken() != null;
    }

    public String getUrl() {
        return this.url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
        this.token = null;
    }

    public void setToken(String token) {
        this.token = token;
        this.username = this.password = null;
    }

}
