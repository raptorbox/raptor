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
package org.createnet.raptor.action;

import org.createnet.raptor.models.data.ActionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class ActionStatusService {
    
    @Autowired
    private ActionStatusRepository repository;
    
    public void save(ActionStatus status) {
        repository.save(status);
    }
    
    public ActionStatus get(String id) {
        return repository.findOne(id);
    }
    
    public ActionStatus get(String deviceId, String actionId) {
        return repository.findOneByDeviceIdAndActionId(deviceId, actionId);
    }
    
    public void delete(String id) {
        repository.delete(id);
    }

    public void delete(ActionStatus status) {
        delete(status.id);
    }

}
