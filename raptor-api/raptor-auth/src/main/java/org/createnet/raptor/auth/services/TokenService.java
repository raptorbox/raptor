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
package org.createnet.raptor.auth.services;

import java.util.List;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.auth.repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class TokenService {

    @Autowired
    private CacheManager cacheManager;
    
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    public class TokenHandlingException extends RuntimeException {

        public TokenHandlingException(Throwable cause) {
            super(cause);
        }
    }

    @Value("${raptor.auth.expiration}")
    private Long expiration;

    @Value("${raptor.auth.secret}")
    private String secret;    
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenUtilService tokenUtil;

    @Autowired
    private TokenRepository tokenRepository;

    /**
     * List token for an user
     *
     * @param uuid
     * @return
     */
    public Iterable<Token> list(String uuid) {
        return tokenRepository.findByUserUuid(uuid);
    }
    
    protected void evictCachedToken(Token token) {
        try {
            cacheManager.getCache("auth_token").evict(token.getToken());
        }
        catch(Exception e){}
    }
    
    protected void putCachedToken(Token token) {
        try {
            cacheManager.getCache("auth_token").put(token.getToken(), token);
        }
        catch(Exception e){}
    }
    
    protected Token getCachedToken(String stoken) {
        
        try {
            return (Token) cacheManager.getCache("auth_token").get(stoken);
        }
        catch(Exception e){}
        
        return null;
    }
    
    /**
     * Load a token by ID
     *
     * @param tokenId
     * @return
     */
    //@Cacheable(key = "#tokenId")
    public Token read(Long tokenId) {
        Token saved = tokenRepository.findOne(tokenId);
        putCachedToken(saved);
        return saved;
    }

    /**
     * Delete a token from database
     *
     * @param token
     */
    public void delete(Token token) {
        tokenRepository.delete(token.getId());
        evictCachedToken(token);
    }

    public void delete(List<Token> list) {
        list.forEach((t) -> delete(t));
    }

    /**
     * Save a token to database
     *
     * @param token
     * @return
     */
    public Token save(Token token) {
       Token saved = tokenRepository.save(token);
       evictCachedToken(saved);
       return read(saved.getId());
    }

    /**
     * Load a token entity based on the token string
     *
     * @param authToken
     * @return
     */
    public Token read(String authToken) {
        
        if (authToken == null || authToken.isEmpty()) {
            return null;
        }
        
        Token cached = getCachedToken(authToken);
        if(cached != null) {
            return cached;
        }
        
        return tokenRepository.findByToken(authToken);
    }

    /**
     * Create a login token
     *
     * @param user
     * @return
     */
    @Retryable(maxAttempts = 5, value = TokenHandlingException.class, backoff = @Backoff(delay = 200, multiplier = 4))
    public Token createLoginToken(User user) {

        logger.debug("Creating login token for user:{}", user.getId());
        Token token = tokenUtil.createToken("login", user, expiration, passwordEncoder.encode(user.getPassword() + this.secret));
        token.setType(Token.Type.LOGIN);

        try {
            token = save(token);
            logger.debug("New token created id:{} for user:{}", token.getId(), user.getId());
            return token;
        } catch (DataIntegrityViolationException e) {
            logger.warn("Failed to store the token, trying to regenerate. err:{}", e.getMessage());
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                // does nothing
            }
            throw new TokenHandlingException(e);
        }

    }

    /**
     * Generate the token string based on the provided secret
     *
     * @param token
     * @return
     */
    public Token generateToken(Token token) {
        tokenUtil.generate(token);
        return token;
    }

    /**
     * Check if the token is valid
     *
     * @param token
     * @param secret
     * @return
     */
    public boolean isValid(Token token, String secret) {
        // Cannot read the token claims?
        if (!tokenUtil.validate(token, secret)) {
            return false;
        }
        return token.isValid();
    }

    /**
     * Check if the token is valid
     *
     * @param token
     * @return
     */
    public boolean isValid(Token token) {
        if (token == null) {
            return false;
        }
        return isValid(token, token.getSecret());
    }

    @Transactional
    public Iterable<Token> findByType(Token.Type type) {
        return tokenRepository.findByType(type);
    }

}
