/*
 * Copyright 2016 Luca Capra <luca.capra@create-net.org>.
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
package org.createnet.raptor.client.model;

import org.createnet.raptor.client.RaptorClient;
import org.createnet.raptor.client.RaptorComponent;

/**
 *
 * @author Luca Capra <luca.capra@create-net.org>
 */
abstract class AbstractClient implements RaptorComponent {
    
    protected String buildQueryString(Integer offset, Integer limit) {
        String qs = null;
        if (offset != null) {
            qs = "?offset=" + offset;
        }
        if (limit != null) {
            if (qs == null) {
                qs = "?";
            } else {
                qs += "&";
            }
            qs += "limit=" + limit;
        }
        return qs == null ? "" : qs;
    }

    private RaptorClient client;

    @Override
    public RaptorClient getClient() {
        return this.client;
    }

    @Override
    public void setClient(RaptorClient client) {
        this.client = client;
    }

}
