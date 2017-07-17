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
package org.createnet.raptor.common.configuration;

import javax.servlet.http.HttpServletRequest;
import org.createnet.raptor.models.configuration.RaptorConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Component
public class TokenHelper {

    @Autowired
    RaptorConfiguration config;

    /**
     * Extract the raw token from the request header
     *
     * @param rawToken
     * @return
     */
    public String extractToken(String rawToken) {
        String tokenPrefix = config.getAuth().getHeaderPrefix();
        if (rawToken.startsWith(tokenPrefix)) {
            rawToken = rawToken.substring(tokenPrefix.length());
        }

        return rawToken.trim();
    }

    String getToken(HttpServletRequest httpRequest) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
