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

import javax.sql.DataSource;
import org.createnet.raptor.auth.services.AclTokenService;
import org.createnet.raptor.auth.services.AuthMessageHandler;
import org.createnet.raptor.common.configuration.TokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Configuration
public class AuthConfiguration {

    @Autowired
    private DataSource dataSource;

    @Autowired
    AclTokenService aclTokenService;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    TokenHelper tokenHelper() {
        return new TokenHelper();
    }

    @Bean
    AuthMessageHandler authMessageHandler() {
        return new AuthMessageHandler();
    }

    @Autowired
    void authenticationManagerBuilder(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource);
    }

}
