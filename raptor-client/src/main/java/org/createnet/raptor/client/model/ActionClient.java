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
package org.createnet.raptor.client.model;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import org.createnet.raptor.client.RaptorClient;
import org.createnet.raptor.client.RaptorComponent;
import org.createnet.raptor.models.data.ActionStatus;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.ServiceObject;

/**
 * Represent a service object action
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class ActionClient extends AbstractClient {

    /**
     * List available actions on the object
     *
     * @param object reference
     * @return return the list of available action for an object
     */
    public List<Action> list(ServiceObject object) {

        List<Action> actions = ServiceObject.getMapper().convertValue(getClient().get(RaptorComponent.format(RaptorClient.Routes.ACTION_LIST, object.id)), new TypeReference<List<Action>>() {
        });

        actions.forEach(action -> {
            action.setServiceObject(object);
        });

        return actions;
    }

    /**
     * Get the action status for an object
     *
     * @param action
     * @return return the list of available action for an object
     */
    public ActionStatus getStatus(Action action) {
        return ServiceObject.getMapper().convertValue(
                getClient().get(
                        RaptorComponent.format(RaptorClient.Routes.ACTION_STATUS, action.getServiceObject().id, action.name)
                ),
                ActionStatus.class
        );
    }

    /**
     * Get the action status for an object
     *
     * @param objectId id of the object
     * @param actionId name of the action
     * @return return the list of available action for an object
     */
    public ActionStatus getStatus(String objectId, String actionId) {
        return ServiceObject.getMapper().convertValue(
                getClient().get(
                        RaptorComponent.format(RaptorClient.Routes.ACTION_STATUS, objectId, actionId)
                ),
                ActionStatus.class
        );
    }

    /**
     * Set the action status for an object
     *
     * @param action the action to set the status
     * @param status the current status
     * @return return the list of available action for an object
     */
    public ActionStatus setStatus(Action action, ActionStatus status) {
        return ServiceObject.getMapper().convertValue(
                getClient().post(
                        RaptorComponent.format(RaptorClient.Routes.ACTION_STATUS, action.getServiceObject().id, action.name), status.toJsonNode()
                ),
                ActionStatus.class
        );
    }

    /**
     * Remove the action status for an object
     *
     * @param action the action to set the status
     */
    public void removeStatus(Action action) {
        getClient().delete(
                RaptorComponent.format(RaptorClient.Routes.ACTION_STATUS, action.getServiceObject().id, action.name)
        );
    }
    
    /**
     * Remove the action status for an object
     *
     * @param objectId id of the object
     * @param actionId name of the action
     */
    public void removeStatus(String objectId, String actionId) {
        getClient().delete(
                RaptorComponent.format(RaptorClient.Routes.ACTION_STATUS, objectId, actionId)
        );
    }

    /**
     * Set the action status for an object
     *
     * @param objectId id of the object
     * @param actionId name of the action
     * @param status the current status
     * @return return the list of available action for an object
     */
    public ActionStatus setStatus(String objectId, String actionId, ActionStatus status) {
        return ServiceObject.getMapper().convertValue(
                getClient().post(
                        RaptorComponent.format(RaptorClient.Routes.ACTION_STATUS, objectId, actionId), status.toJsonNode()
                ),
                ActionStatus.class
        );
    }

    /**
     * Invoke an action on the object
     *
     * @param action the action reference
     * @param payload the payload to send as string
     */
    public void invoke(Action action, String payload) {
        getClient().post(
                RaptorComponent.format(RaptorClient.Routes.INVOKE, action.getServiceObject().id, action.name), payload
        );
    }

}
