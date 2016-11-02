/*
 * Copyright 2016 CREATE-NET
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
 * Represent a virtual object
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class Action
        extends org.createnet.raptor.models.objects.Stream
        implements RaptorComponent {

    private RaptorClient client;

    public Action() {
    }

    @Override
    public RaptorClient getClient() {
        return this.client;
    }

    @Override
    public void setClient(RaptorClient client) {
        this.client = client;
    }

}
