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

import org.createnet.raptor.auth.token.EncodedTokenService;
import org.createnet.raptor.auth.token.JwtTokenService;
import org.createnet.raptor.auth.token.TokenGenerator;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class TokenUtilService implements TokenGenerator {

    private static final Logger logger = LoggerFactory.getLogger(TokenUtilService.class);

    @Autowired
    private JwtTokenService jwt;

    @Autowired
    private EncodedTokenService def;

    protected TokenGenerator getImpl(Token t) {
        switch (t.getTokenType()) {
            case JWT:
                return jwt;
            case DEFAULT:
            default:
                return def;
        }
    }

    @Override
    public String generate(Token t) {
        return getImpl(t).generate(t);
    }

    @Override
    public boolean validate(Token t, String secret) {
        return getImpl(t).validate(t, secret);
    }

    public Token createToken(String name, User user, Long expires, String secret) {

        Token token = new Token();
        token.setUser(user);
        token.setExpires(expires);
        token.setSecret(secret);
        token.setName(name);
        token.setEnabled(true);

        final String tokenValue = generate(token);
        return token;
    }

}
