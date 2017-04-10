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
package org.createnet.raptor.http.api;

import javax.inject.Inject;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.service.tools.AuthService;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.service.core.ActionManagerService;
import org.createnet.raptor.service.core.ObjectManagerService;
import org.createnet.raptor.service.core.StreamManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
abstract public class AbstractApi {

    final private Logger logger = LoggerFactory.getLogger(AbstractApi.class);
    
    @Inject
    protected ObjectManagerService objectManager;
    
    @Inject
    protected StreamManagerService streamManager;
    
    @Inject
    protected ActionManagerService actionManager;
   
    @Inject
    protected AuthService auth;    
    
    /**
     * @param obj
     * @param op
     * @return 
     * @deprecated move the auth api to listen on broker events
     */
    @Deprecated
    protected boolean syncObject(Device obj, Authentication.SyncOperation op) {
        try {
            auth.sync(auth.getAccessToken(), obj, op);
        } catch (Exception ex) {
            logger.error("Error syncing object to auth system: {}", ex.getMessage());
            return false;
        }
        return true;
    }    
    
}
