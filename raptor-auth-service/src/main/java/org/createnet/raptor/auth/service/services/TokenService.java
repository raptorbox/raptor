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
package org.createnet.raptor.auth.service.services;

import java.util.List;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.auth.service.repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    public class TokenHandlingException extends RuntimeException {

        public TokenHandlingException(Throwable cause) {
            super(cause);
        }
    }

    @Value("${raptor.auth.secret}")
    private String secret;

    @Value("${raptor.auth.expiration}")
    private Long expiration;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private TokenUtilService tokenUtil;

    @Autowired
    private TokenRepository tokenRepository;

    public Iterable<Token> list(String uuid) {
        return tokenRepository.findByUserUuid(uuid);
    }

    public Token read(Long tokenId) {
        return tokenRepository.findOne(tokenId);
    }

    public void delete(Token token) {
        Token t2 = tokenRepository.findOne(token.getId());
        if (t2 == null) {
            return;
        }
        tokenRepository.delete(t2.getId());
    }

    public Token update(Token rawToken) {

        Token token = read(rawToken.getId());
        if (token == null) {
            return null;
        }

        token.setName(rawToken.getName());

        tokenRepository.save(token);
        return token;
    }

    public Token create(Token token) {
        tokenRepository.save(token);
        return token;
    }

    public Token read(String authToken) {
        if (authToken == null) {
            return null;
        }
        return tokenRepository.findByToken(authToken);
    }

    /**
     * Create a login token
     * @param user
     * @return
     */
    @Retryable(maxAttempts = 5, value = TokenHandlingException.class, backoff = @Backoff(delay = 200, multiplier = 4))
    public Token createLoginToken(User user) {

        logger.debug("Creating login token for user:{}", user.getId());

        List<Token> tokens = tokenRepository.findByTypeAndUser(Token.Type.LOGIN, user);
        if (tokens.size() > 1) {
//            Token validtoken = null;
            for (Token loginToken : tokens) {
//                if (loginToken.isValid() && validtoken == null) {
//                    validtoken = loginToken;
//                    continue;
//                }
                if (!loginToken.isValid()) {
                    logger.debug("Drop previous login token id:{} for user:{}", loginToken.getId(), user.getId());
                    try {
                        delete(loginToken);
                    } catch (Exception ex) {
                        logger.warn("Error deleting token id:{}, err:{}", loginToken.getId(), ex.getMessage());
                    }
                }
            }
//            if (validtoken != null) {
//                logger.debug("Reused valid login token id:{} for user:{}", validtoken.getId(), user.getId());
//                return validtoken;
//            }
        }

        Token token = tokenUtil.createToken("login", user, expiration, passwordEncoder.encode(user.getPassword() + this.secret));
        token.setType(Token.Type.LOGIN);

        try {
            create(token);
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

    public Token generateToken(Token token) {
        tokenUtil.generate(token);
        return token;
    }

    public boolean isValid(Token token, String secret) {
        // Cannot read the token claims?
        if (!tokenUtil.validate(token, secret)) {
            return false;
        }
        return token.isValid();
    }

    public boolean isValid(Token token) {
        if (token == null) {
            return false;
        }
        return isValid(token, token.getSecret());
    }

}
