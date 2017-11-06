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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.createnet.raptor.models.auth.Role;
import org.createnet.raptor.sdk.AbstractClient;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.RequestOptions;
import org.createnet.raptor.sdk.Routes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods to interact with Raptor API
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class GroupClient extends AbstractClient {

    public GroupClient(Raptor container) {
        super(container);
    }

    final static Logger logger = LoggerFactory.getLogger(GroupClient.class);

    /**
     * Get user groups
     *
     * @param userUuid group owner
     * @return
     */
    public List<Role> list(String userUuid) {
        JsonNode node = getClient().get(String.format(Routes.GROUP_LIST, userUuid));
        return getMapper().convertValue(node, new TypeReference<List<Role>>() {
        });
    }

    /**
     * Get current user group
     *
     * @return
     */
    public List<Role> list() {
        return list(getContainer().Auth().getUser().getUuid());
    }

    /**
     * Read a group
     *
     * @param groupId
     * @return
     */
    public Role read(long groupId) {
        JsonNode node = getClient().get(String.format(Routes.GROUP_READ, groupId));
        Role t1 = getMapper().convertValue(node, Role.class);
        return t1;
    }

    /**
     * Read a group
     *
     * @param group
     * @return
     */
    public Role read(Role group) {
        assert group != null;
        assert group.getId() != null;
        return read(group.getId());
    }

    /**
     * Create a new group
     *
     * @param group
     * @return
     */
    public Role create(Role group) {
        return create(group, null);
    }

    public Role create(Role group, RequestOptions opts) {

        JsonNode node = getClient().post(Routes.GROUP_CREATE, toJsonNode(group), opts);
        Role t1 = getMapper().convertValue(node, Role.class);

        group.merge(t1);
        group.setId(t1.getId());

        return group;
    }

    /**
     * Delete a group
     *
     * @param group
     */
    public void delete(Role group) {
        delete(group.getId());
    }

    /**
     * Delete a group
     *
     * @param groupId
     */
    public void delete(Long groupId) {
        getClient().delete(String.format(Routes.GROUP_DELETE, groupId.toString()));
    }

    /**
     * Update a group
     *
     * @param group
     * @return
     */
    public Role update(Role group) {

        JsonNode node = getClient().put(String.format(Routes.GROUP_UPDATE, group.getId()), toJsonNode(group));
        Role t1 = getMapper().convertValue(node, Role.class);

        group.merge(t1);
        group.setId(t1.getId());

        return group;
    }

}
