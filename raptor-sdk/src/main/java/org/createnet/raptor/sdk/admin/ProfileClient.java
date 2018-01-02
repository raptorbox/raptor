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
package org.createnet.raptor.sdk.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.createnet.raptor.sdk.AbstractClient;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.Routes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods to interact with Raptor API
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class ProfileClient extends AbstractClient {

    public ProfileClient(Raptor container) {
        super(container);
    }

    final static Logger logger = LoggerFactory.getLogger(ProfileClient.class);

    /**
     * Set profile data
     *
     * @param name
     * @param data
     * @return
     */
    public JsonNode set(String name, Object data) {
        return getClient().put(String.format(Routes.PROFILE_SET, getContainer().Auth().getUser().getId(), name), toJsonNode(data));
    }
    
    /**
     * Get profile data
     *
     * @param name
     * @return
     */
    public JsonNode get(String name) {
        return getClient().get(String.format(Routes.PROFILE_GET, getContainer().Auth().getUser().getId(), name));
    }
    
    /**
     * Get all profile data
     *
     * @return
     */
    public JsonNode get() {
        return getClient().get(String.format(Routes.PROFILE_GET_ALL, getContainer().Auth().getUser().getId()));
    }

    public ObjectNode newObjectNode() {
        return getMapper().createObjectNode();
    }

}
