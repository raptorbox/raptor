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
package org.createnet.raptor.auth.token;

import org.createnet.raptor.models.auth.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Component
public class EncodedTokenService implements TokenGenerator {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public String generate(Token t) {
        String hash = passwordEncoder.encode(t.getSecret());
        t.setToken(hash);
        return hash;
    }

    @Override
    public boolean validate(Token t, String secret) {
        return passwordEncoder.matches(secret, t.getToken());
    }
    
}
