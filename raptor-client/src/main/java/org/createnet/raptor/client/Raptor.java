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
package org.createnet.raptor.client;

import org.createnet.raptor.client.model.ActionClient;
import org.createnet.raptor.client.model.AuthClient;
import org.createnet.raptor.client.model.ServiceObjectClient;
import org.createnet.raptor.client.model.StreamClient;

/**
 * Raptor SDK wrapper entry point
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public class Raptor {

    final protected RaptorClient client;

    final private AuthClient auth;
    final private StreamClient stream;
    final private ActionClient action;
    final private ServiceObjectClient serviceObject;

    public RaptorClient getClient() {
        return client;
    }
    
    /**
     * Get a client for the user manager
     * @return
     */
    public AuthClient auth() {
        return auth;
    }

    /**
     * Get a client to manage data streams
     * @return
     */
    public StreamClient stream() {
        return stream;
    }

    /**
     * Get a client to manage actuations
     * @return
     */
    public ActionClient action() {
        return action;
    }

    /**
     * Get a client to manage object definitions
     * @return
     */    
    public ServiceObjectClient serviceObject() {
        return serviceObject;
    }

    public Raptor(RaptorClient.ClientConfig config) {

        client = new RaptorClient(config);

        auth = new AuthClient();
        auth.setClient(client);

        stream = new StreamClient();
        stream.setClient(client);

        serviceObject = new ServiceObjectClient();
        serviceObject.setClient(client);

        action = new ActionClient();
        action.setClient(client);

    }

}
