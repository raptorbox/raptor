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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.stream.Collectors;
import org.createnet.raptor.models.auth.Permission;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.request.PermissionRequestBatch;
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
public class TokenPermissionClient extends AbstractClient {

    public TokenPermissionClient(Raptor container) {
        super(container);
    }

    final static Logger logger = LoggerFactory.getLogger(TokenPermissionClient.class);

    /**
     * Get user token
     *
     * @param tokenId token ID
     * @return
     */
    public List<String> get(String tokenId) {
        JsonNode node = getClient().get(String.format(Routes.PERMISSION_GET, "token", tokenId));
        return getMapper().convertValue(node, new TypeReference<List<String>>() {
        });
    }

    /**
     * Get user token
     *
     * @param token token
     * @return
     */
    public List<String> get(Token token) {
        return get(token.getId());
    }

    /**
     * Set permission for a token
     *
     * @param token
     * @param permissions
     * @return 
     */
    public List<String> set(Token token, List<Permission> permissions) {
        return set(token.getId(), permissions);
    }

    /**
     * Set permission for a token
     *
     * @param tokenId
     * @param permissions
     * @return
     */
    public List<String> set(String tokenId, List<Permission> permissions) {
        
        PermissionRequestBatch req = new PermissionRequestBatch();
        req.permissions = permissions.stream()
                .map((p) -> p.getName())
                .collect(Collectors.toList());
        
        JsonNode node = getClient().put(String.format(Routes.PERMISSION_SET, "token", tokenId), toJsonNode(req));
        return getMapper().convertValue(node, new TypeReference<List<String>>() {
        });
    }

}
