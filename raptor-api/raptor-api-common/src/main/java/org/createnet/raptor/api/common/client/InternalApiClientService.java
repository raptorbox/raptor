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
package org.createnet.raptor.api.common.client;

import org.createnet.raptor.api.common.BaseApplication;
import org.createnet.raptor.sdk.Raptor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@Component
public class InternalApiClientService extends Raptor {

    public InternalApiClientService(String url, String username, String password) {
        super(url, username, password);
    }

    @Scheduled(fixedRate = 10000)
    public void refreshToken() {

        if (Auth().getConfig().getUsername() == null) {
            return;
        }
        
        if(Auth().getToken() == null) {
            BaseApplication.log.debug("Missing service token, skip refresh");
            Auth().login();
            return;
        }
        
        BaseApplication.log.debug("Refreshing service token");
        Auth().refreshToken();
        BaseApplication.log.debug("Service token updated");

    }

}
