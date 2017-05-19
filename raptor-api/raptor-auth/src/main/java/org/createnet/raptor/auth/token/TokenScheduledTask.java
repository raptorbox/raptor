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

import java.util.ArrayList;
import java.util.List;
import org.createnet.raptor.auth.services.TokenService;
import org.createnet.raptor.models.auth.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Component
public class TokenScheduledTask {
    
    private Logger log = LoggerFactory.getLogger(TokenScheduledTask.class);
    
    @Autowired
    private TokenService tokenService;

    @Scheduled(fixedRate = (1000*60*5)) // every 5min
    public void removeInvalidToken() {
        
        log.debug("Checking for invalid login tokens");
        
        List<Token> remove = new ArrayList();
        Iterable<Token> list = tokenService.findByType(Token.Type.LOGIN);
        for (Token token : list) {
            if (!token.isValid()) {
                remove.add(token);
            }
        }
        
        if(!remove.isEmpty()) {
            try {
                log.debug("Removing {} tokens", remove.size());
                tokenService.delete(remove);
                log.debug("Done");
            }
            catch(Exception ex) {
                log.warn("Failed to remove invalid tokens: {}", ex.getMessage());
            }
        }
    }

}
