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
package org.createnet.raptor.api.common.authentication;

import java.util.Collection;
import org.createnet.raptor.models.auth.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class LoginAuthenticationToken extends UsernamePasswordAuthenticationToken {
    
    private final String token;
    private final User user;
    
    public LoginAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        token = (String)credentials;
        user = (User)principal;
    }
    
    public String getToken() {
        return token;
    }
    
    public User getUser() {
        return user;
    }
}
