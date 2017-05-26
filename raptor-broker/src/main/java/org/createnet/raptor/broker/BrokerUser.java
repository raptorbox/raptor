/*
 * Copyright 2017 Luca Capra <lcapra@fbk.eu>.
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
package org.createnet.raptor.broker;

import org.createnet.raptor.models.auth.Role;
import org.createnet.raptor.models.configuration.BrokerLocalUser;
import org.createnet.raptor.sdk.Raptor;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class BrokerUser {
        
    private BrokerLocalUser localUser;
    private Raptor raptor;

    public BrokerUser() {
    }
    
    public BrokerUser(Raptor raptor) {
        this.raptor = raptor;
    }
    
    public BrokerUser(BrokerLocalUser localUser) {
        this.localUser = localUser;
    }

    public Raptor getRaptor() {
        return raptor;
    }
    
    public boolean isAdmin() {
        if(isLocal()) 
            return getLocalUser().getRoles().contains(Role.Roles.admin.name());
        else 
            return getRaptor().Auth().getUser().isAdmin();
    }
    
    public boolean isLocal() {
        return getLocalUser() != null;
    }

    public BrokerLocalUser getLocalUser() {
        return localUser;
    }
    
}
