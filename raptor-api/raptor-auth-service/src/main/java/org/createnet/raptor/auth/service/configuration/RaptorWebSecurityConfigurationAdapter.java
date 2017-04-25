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
package org.createnet.raptor.auth.service.configuration;

import org.createnet.raptor.auth.service.services.RaptorUserDetailsService;
import org.createnet.raptor.auth.service.authentcation.JsonUsernamePasswordFilter;
import org.createnet.raptor.auth.service.authentcation.RaptorAuthenticationEntryPoint;
import org.createnet.raptor.auth.service.authentcation.RaptorAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class RaptorWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

    @Value("${raptor.auth.route.authentication.path}")
    private String authenticationPath;

    @Value("${raptor.auth.route.authentication.refresh}")
    private String authenticationRefresh;

    @Autowired
    private RaptorAuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    private RaptorUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(this.userDetailsService)
                .passwordEncoder(passwordEncoder);
    }

    @Bean
    public RaptorAuthenticationTokenFilter authenticationTokenFilterBean() throws Exception {
        return new RaptorAuthenticationTokenFilter();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // we don't need CSRF because our token is invulnerable
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                // don't create session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .cors().and()
                .authorizeRequests()
                .antMatchers(authenticationPath).permitAll()
                .antMatchers(authenticationRefresh).permitAll()
                .antMatchers("/v2/api-docs").permitAll()
                // keep this method private to allow sync beetween api and auth
                .antMatchers("/sync").hasIpAddress("127.0.0.1")
                .anyRequest().authenticated();

        // Custom JWT based security filter
        httpSecurity.addFilterBefore(authenticationTokenFilterBean(), JsonUsernamePasswordFilter.class);

        // disable page caching
        httpSecurity.headers().cacheControl();
    }
}
