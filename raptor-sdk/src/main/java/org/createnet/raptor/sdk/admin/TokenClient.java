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
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.createnet.raptor.models.payload.TokenPayload;
import org.createnet.raptor.sdk.AbstractClient;
import org.createnet.raptor.sdk.PageResponse;
import org.createnet.raptor.sdk.QueryString;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.RequestOptions;
import org.createnet.raptor.sdk.Routes;
import org.createnet.raptor.sdk.events.callback.TokenCallback;
import org.createnet.raptor.sdk.events.callback.TokenEventCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods to interact with Raptor API
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class TokenClient extends AbstractClient {

    protected class JsonToken extends Token {

        public JsonToken() {
        }

        public JsonToken(Token token) {
            super(token);
        }

        @JsonProperty(access = JsonProperty.Access.READ_WRITE)
        protected String secret;
    }

    protected TokenPermissionClient Permission;

    public TokenPermissionClient Permission() {
        if (Permission == null) {
            Permission = new TokenPermissionClient(getContainer());
        }
        return Permission;
    }

    public TokenClient(Raptor container) {
        super(container);
    }

    final static Logger logger = LoggerFactory.getLogger(TokenClient.class);


    /**
     * Register for token events
     *
     * @param token
     * @param callback The callback to fire on event arrival
     */
    public void subscribe(Token token, TokenEventCallback callback) {
        getEmitter().subscribe(token, callback);
    }

    /**
     * Subscribe to token related events 
     *
     * @param token
     * @param ev
     */
    public void subscribe(Token token, TokenCallback ev) {
        getEmitter().subscribe(token, (DispatcherPayload payload) -> {
            switch (payload.getType()) {
                case token:
                    ev.callback(token, (TokenPayload) payload);
                    break;
            }
        });
    }    
    
    /**
     * Get user tokens
     *
     * @param userUuid token owner
     * @param page
     * @param limit
     * @return
     */
    public PageResponse<Token> list(String userUuid, int page, int limit) {
        QueryString qs = new QueryString();
        qs.query.add("userId", userUuid);
        qs.pager.page = page;
        qs.pager.size = limit;
        JsonNode node = getClient().get(Routes.TOKEN_LIST + qs.toString());
        return getMapper().convertValue(node, new TypeReference<PageResponse<Token>>() {});
    }
    
    /**
     * Get user tokens
     *
     * @param userUuid token owner
     * @return
     */
    public PageResponse<Token> list(String userUuid) {
        QueryString qs = new QueryString();
        qs.query.add("userId", userUuid);
        JsonNode node = getClient().get(Routes.TOKEN_LIST + qs.toString());
        return getMapper().convertValue(node, new TypeReference<PageResponse<Token>>() {});
    }

    /**
     * Get current user token
     *
     * @return
     */
    public PageResponse<Token> list() {
        return list(getContainer().Auth().getUser().getId());
    }
    
    /**
     * Read a token
     *
     * @param tokenId
     * @return
     */
    public Token read(String tokenId) {
        JsonNode node = getClient().get(String.format(Routes.TOKEN_GET, tokenId));
        Token t1 = getMapper().convertValue(node, Token.class);
        return t1;
    }
    
    /**
     * Get details on the current token
     *
     * @return
     */
    public Token current() {
        JsonNode node = getClient().get(Routes.TOKEN_CURRENT);
        Token t1 = getMapper().convertValue(node, Token.class);
        return t1;
    }

    /**
     * Create a new token
     *
     * @param token
     * @return
     */
    public Token create(Token token) {
        return create(token, null);
    }
    
    public Token create(Token token, RequestOptions opts) {
        JsonToken jsonToken = new JsonToken(token);
        JsonNode node = getClient().post(Routes.TOKEN_CREATE, toJsonNode(jsonToken), opts);
        Token t1 = getMapper().convertValue(node, Token.class);
        return mergeToken(token, t1);
    }

    /**
     * Update a token
     *
     * @param token
     * @return
     */
    public Token update(Token token) {

        JsonToken jsonToken = new JsonToken(token);
        JsonNode node = getClient().put(String.format(Routes.TOKEN_UPDATE, token.getId()), toJsonNode(jsonToken));
        Token t1 = getMapper().convertValue(node, Token.class);

        return mergeToken(token, t1);
    }

    private Token mergeToken(Token token, Token t1) {
        token.merge(t1);
        token.setToken(t1.getToken());
        token.setId(t1.getId());
        return token;
    }

}
