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
package org.createnet.raptor.client;

import org.createnet.raptor.client.model.ActionClient;
import org.createnet.raptor.client.model.ServiceObjectClient;
import org.createnet.raptor.client.model.StreamClient;

/**
 * Raptor SDK wrapper entry point
 * 
 * @author Luca Capra <luca.capra@create-net.org>
 */
public class Raptor {
    
    final protected RaptorClient client;
    
    final public StreamClient stream;
    final public ActionClient action;
    final public ServiceObjectClient serviceObject;
    
    public Raptor(RaptorClient.RaptorConfig config) {
        
        client = new RaptorClient(config);
        
        stream = new StreamClient();
        stream.setClient(client);
        
        serviceObject = new ServiceObjectClient();
        serviceObject.setClient(client);
        
        action = new ActionClient();
        action.setClient(client);

    }

}
