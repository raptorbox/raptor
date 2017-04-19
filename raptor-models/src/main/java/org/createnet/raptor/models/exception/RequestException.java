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
package org.createnet.raptor.models.exception;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class RequestException extends RuntimeException {

    public int status;
    public String statusText;
    public String body;

    public RequestException(int status, String statusText, String body) {
        this.status = status;
        this.statusText = statusText;
        this.body = body;
    }

    @Override
    public String getMessage() {
        return String.format("HTTP %d %s - %s", getStatus(), getStatusText(), getBody());
    }

    public int getStatus() {
        return status;
    }

    public String getStatusText() {
        return statusText;
    }

    public String getBody() {
        return body;
    }

}
