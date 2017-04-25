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
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.sdk.AbstractClient;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.api.HttpClient;
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
    
    final public TokenPermissionClient Permission;
    
    public TokenClient(Raptor container) {
        super(container);
        Permission = new TokenPermissionClient(container);
    }

    final static Logger logger = LoggerFactory.getLogger(TokenClient.class);

    /**
     * Get user token
     *
     * @param userUuid token owner
     * @return
     */
    public List<Token> get(String userUuid) {
        JsonNode node = getClient().get(String.format(HttpClient.Routes.TOKEN_GET, userUuid));
        return getMapper().convertValue(node, new TypeReference<List<Token>>() {});
    }
    
    /**
     * Get current user token
     *
     * @return
     */
    public List<Token> get() {
        JsonNode node = getClient().get(String.format(HttpClient.Routes.TOKEN_GET, getContainer().Auth().getUser().getUuid()));
        return getMapper().convertValue(node, new TypeReference<List<Token>>() {});
    }

    /**
     * Create a new token
     * @param token
     * @return
     */
    public Token create(Token token) {
        JsonToken jsonToken = new JsonToken(token);
        JsonNode node = getClient().post(HttpClient.Routes.TOKEN_CREATE, toJsonNode(jsonToken));
        Token t1 = getMapper().convertValue(node, Token.class);
        return mergeToken(token, t1);
    }

    /**
     * Update a token
     * @param token
     * @return
     */
    public Token update(Token token) {

        JsonToken jsonToken = new JsonToken(token);
        JsonNode node = getClient().put(String.format(HttpClient.Routes.TOKEN_UPDATE, token.getId()), toJsonNode(jsonToken));
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
