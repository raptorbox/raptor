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
import org.createnet.raptor.sdk.PageResponse;
import org.createnet.raptor.sdk.QueryString;
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
public class RoleClient extends AbstractClient {

    public RoleClient(Raptor container) {
        super(container);
    }

    final static Logger logger = LoggerFactory.getLogger(RoleClient.class);

    /**
     * Get user groups
     *
     * @param userUuid group owner
     * @param page
     * @param limit
     * @return
     */
    public PageResponse<Role> list(String userUuid, int page, int limit) {
        QueryString qs = new QueryString();
        qs.query.add("userId", userUuid);
        qs.pager.page = page;
        qs.pager.size = limit;
        return list(qs);
    }

    /**
     * Get current user group
     *
     * @return
     */
    public PageResponse<Role> list() {
        return list(getContainer().Auth().getUser().getId(), 0, 100);
    }

    /**
     * Get current user group
     *
     * @param qs
     * @return
     */
    public PageResponse<Role> list(QueryString qs) {
        JsonNode node = getClient().get(Routes.ROLE_LIST + qs.toString());
        return getMapper().convertValue(node, new TypeReference<PageResponse<Role>>() {
        });
    }

    /**
     * Read a group
     *
     * @param groupId
     * @return
     */
    public Role read(String groupId) {
        JsonNode node = getClient().get(String.format(Routes.ROLE_READ, groupId));
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

        JsonNode node = getClient().post(Routes.ROLE_CREATE, toJsonNode(group), opts);
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
    public void delete(String groupId) {
        getClient().delete(String.format(Routes.ROLE_DELETE, groupId.toString()));
    }

    /**
     * Update a group
     *
     * @param group
     * @return
     */
    public Role update(Role group) {

        JsonNode node = getClient().put(String.format(Routes.ROLE_UPDATE, group.getId()), toJsonNode(group));
        Role t1 = getMapper().convertValue(node, Role.class);

        group.merge(t1);
        group.setId(t1.getId());

        return group;
    }

}
