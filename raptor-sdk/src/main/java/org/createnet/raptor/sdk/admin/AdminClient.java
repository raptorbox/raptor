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

import org.createnet.raptor.sdk.AbstractClient;
import org.createnet.raptor.sdk.Raptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods to interact with Raptor API
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class AdminClient extends AbstractClient {

    protected UserClient User;
    protected TokenClient Token;
    protected RoleClient Role;

    public UserClient User() {
        if (User == null) {
            User = new UserClient(getContainer());
        }
        return User;
    }

    public RoleClient Role() {
        if (Role == null) {
            Role = new RoleClient(getContainer());
        }
        return Role;
    }

    public TokenClient Token() {
        if (Token == null) {
            Token = new TokenClient(getContainer());
        }
        return Token;
    }

    public AdminClient(Raptor container) {
        super(container);
    }

    final static Logger logger = LoggerFactory.getLogger(AdminClient.class);

}
