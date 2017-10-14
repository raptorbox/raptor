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
package org.createnet.raptor.common.client;

import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.api.AuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@Component
public class InternalApiClientService extends RaptorSdk {
    
    private Logger log = LoggerFactory.getLogger(InternalApiClientService.class);

    public InternalApiClientService(String url, String token) {
        super(url, token);
    }
    
    public Raptor impersonate(String userId) {
        AuthClient.LoginState state = Admin().User().impersonate(userId);
        return new Raptor(config.getUrl(), state.token);
    }

    public Raptor impersonate(User user) {
        return impersonate(user.getUuid());
    }
    
}
