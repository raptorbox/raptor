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

import java.util.Optional;
import javax.annotation.PostConstruct;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.configuration.AuthConfiguration;
import org.createnet.raptor.models.configuration.RaptorConfiguration;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.RequestOptions;
import org.createnet.raptor.sdk.api.AuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@Component
public class InternalApiClientService extends RaptorSdk {

    private Logger log = LoggerFactory.getLogger(InternalApiClientService.class);
    
    @Autowired
    protected RaptorConfiguration configuration;
    
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

    @PostConstruct
    public void onInitialize() {

        log.debug("Retrieving service token");

        AuthConfiguration.AdminUser serviceUser = configuration.getAuth().getServiceUser();
        
        this.setCredentials(serviceUser.getUsername(), serviceUser.getPassword());
        
        // retry with big delay to ensure the system is up                
        RequestOptions reqOpts = RequestOptions.retriable().maxRetries(5).waitFor(5000);
        this.Auth().login(reqOpts);
        
        User u = this.Auth().getUser();
        
        String tokenName = configuration.getAuth().getDefaultToken();
        Optional<Token> found = this.Admin().Token().list().getContent().stream().filter((t) -> tokenName.equals(t.getName())).findFirst();
        
        Token token = found.isPresent() ? found.get() : new Token();
        if (!found.isPresent()) {

            token.setUser(u);
            token.setEnabled(true);
            token.setExpires(0L);
            token.setName(tokenName);

            token.setSecret(u.getPassword());
            token.setType(Token.Type.DEFAULT);

            token = this.Admin().Token().create(token);
            log.debug("Created `{}` token (id={})", token.getName(), token.getId());            
        }

        serviceUser.setToken(token.getToken());
        this.getConfig().setToken(token.getToken());

    }

}
