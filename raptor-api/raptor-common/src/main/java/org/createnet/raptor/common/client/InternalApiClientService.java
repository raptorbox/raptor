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
import org.createnet.raptor.models.exception.RequestException;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.RequestOptions;
import org.createnet.raptor.sdk.api.AuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@Component
public class InternalApiClientService extends Raptor {
    
    private Logger log = LoggerFactory.getLogger(InternalApiClientService.class);
    
    public InternalApiClientService(String url, String username, String password) {
        super(url, username, password);
    }

    @Scheduled(fixedRate = 300*1000)
    public void refreshToken() {

        if (Auth().getConfig().getUsername() == null) {
            return;
        }
        
        if(Auth().getToken() == null) {
            log.debug("Missing service token, attempt login");
            try {
                // retry and wait to handle long bootstrap times
                Auth().login(RequestOptions.retriable().maxRetries(10).waitFor(500));
                log.debug("Service login done");
            }
            catch(Exception ex) {
                log.warn("Service login failed: {}", ex.getMessage());
            }
            
            return;
        }
        
        log.debug("Refreshing service token");
        try {
            Auth().refreshToken();
        }
        catch(RequestException e) {
            if (e.status == 401) {
                log.debug("Service token expired, force new login");
                Auth().getConfig().setToken(null);
                refreshToken();
                return;
            }
        }
        log.debug("Service token updated");

    }

    public Raptor impersonate(String userId) {
        AuthClient.LoginState state = Admin().User().impersonate(userId);
        return new Raptor(config.getUrl(), state.token);
    }

    public Raptor impersonate(User user) {
        return impersonate(user.getUuid());
    }
    
}
