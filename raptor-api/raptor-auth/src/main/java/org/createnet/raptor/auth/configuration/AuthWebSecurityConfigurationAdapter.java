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
package org.createnet.raptor.auth.configuration;

import org.createnet.raptor.auth.services.RaptorUserDetailsService;
import org.createnet.raptor.auth.authentcation.DatabaseTokenFilter;
import org.createnet.raptor.auth.services.TokenService;
import org.createnet.raptor.common.configuration.TokenHelper;
import org.createnet.raptor.common.configuration.TokenSecurityConfigurerAdapter;
import org.createnet.raptor.models.configuration.RaptorConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(5)
@ConditionalOnExpression("{'auth', 'standalone'}.contains('${spring.config.name}')")
public class AuthWebSecurityConfigurationAdapter extends TokenSecurityConfigurerAdapter {

    @Autowired
    private RaptorUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private TokenHelper tokenHelper;

    @Autowired
    public RaptorConfiguration config;
    
    
    protected DatabaseTokenFilter databaseTokenFilter() {
        return new DatabaseTokenFilter(config, tokenService, tokenHelper);
    }
    
    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        configureShared(http);
        http.addFilterBefore(databaseTokenFilter(), AnonymousAuthenticationFilter.class);
    }
    
}
