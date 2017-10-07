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
package org.createnet.raptor.sdk;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class RequestOptions {
    
    protected boolean retry = false;
    protected int maxRetry = 3;
    protected int waitFor = 150;
    
    protected boolean auth = true;
    protected boolean textBody = false;
    
//    protected int timeoutRequest = 3*1000;
//    protected int timeoutSocket = 5*1000;
//
//    public int getTimeoutRequest() {
//        return timeoutRequest;
//    }
//
//    public int getTimeoutSocket() {
//        return timeoutSocket;
//    }
    
    public int getMaxRetry() {
        return maxRetry;
    }

    public int getWaitFor() {
        return waitFor;
    }
    
    public boolean withRetry() {
        return retry;
    }

    public boolean withAuth() {
        return auth;
    }

    public boolean withTextBody() {
        return textBody;
    }
    
    public RequestOptions retry(boolean retry) {
        this.retry = retry;
        return this;
    }
    
    public RequestOptions withAuthToken(boolean auth) {
        this.auth = auth;
        return this;
    }
    
    public RequestOptions textBody(boolean textBody) {
        this.textBody = textBody;
        return this;
    }
    
//    public RequestOptions timeoutRequest(int n) {
//        this.timeoutRequest = n;
//        return this;
//    }
//    
//    public RequestOptions timeoutSocket(int n) {
//        this.timeoutSocket = n;
//        return this;
//    }
    
    public RequestOptions maxRetries(int n) {
        this.maxRetry = n;
        return this;
    }
    
    public RequestOptions waitFor(int n) {
        this.waitFor = n;
        return this;
    }
    
    public static RequestOptions defaults() {
        return new RequestOptions();
    }
    
    public static RequestOptions retriable() {
        return defaults().retry(true);
    }
    
}
