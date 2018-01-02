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

import org.createnet.raptor.common.authentication.RestTokenFilter;
import org.createnet.raptor.models.configuration.RaptorConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(10)
@ConditionalOnExpression("!({'auth'}.contains('${spring.config.name}'))")
public class TokenSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Autowired
    AuthenticationEntryPoint authenticationEntryPoint;   
    
    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }
    
    @Autowired
    public TokenHelper tokenHelper;

    @Autowired
    public RaptorConfiguration config;    
    
    RestTokenFilter restTokenFilter() {
        return new RestTokenFilter(config, tokenHelper);
    }
    
    protected void configureShared(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .cors().and()
                .authorizeRequests()
                .antMatchers("/auth/login").permitAll()
                .anyRequest().authenticated()
                .and().headers().cacheControl();
        
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        configureShared(http);
        http.addFilterBefore(restTokenFilter(), AnonymousAuthenticationFilter.class);
    }

}
