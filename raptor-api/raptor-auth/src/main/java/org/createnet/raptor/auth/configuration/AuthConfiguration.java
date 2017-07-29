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

import java.util.List;
import javax.sql.DataSource;
import org.createnet.raptor.auth.repository.UserRepository;
import org.createnet.raptor.auth.services.AuthMessageHandler;
import org.createnet.raptor.auth.services.UserService;
import static org.createnet.raptor.common.BaseApplication.log;
import org.createnet.raptor.common.configuration.TokenHelper;
import org.createnet.raptor.models.auth.Role;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.configuration.RaptorConfiguration;
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
    private RaptorConfiguration configuration;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TokenHelper tokenHelper() {
        return new TokenHelper();
    }

    @Bean
    AuthMessageHandler authMessageHandler() {
        return new AuthMessageHandler();
    }


    @Autowired
    public void authenticationManagerBuilder(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource);
        createDefaultUser();
    }

    protected void createDefaultUser() {

        List<org.createnet.raptor.models.configuration.AuthConfiguration.AdminUser> users = configuration.getAuth().getUsers();

        users.forEach((org.createnet.raptor.models.configuration.AuthConfiguration.AdminUser admin) -> {

            User defaultUser = userRepository.findByUsername(admin.getUsername());
            if (defaultUser != null) {
                log.debug("User `{}` exists (id: {})", defaultUser.getUsername(), defaultUser.getId());
                
                if (admin.isLocked()) {
                    log.debug("Recreating user `{}`", defaultUser.getUsername());    
                    userService.delete(defaultUser);
                }
                else {
                    return;
                }
            }

            User adminUser = new User();

            adminUser.setUsername(admin.getUsername());
            adminUser.setPassword(passwordEncoder().encode(admin.getPassword()));
            adminUser.setEmail(admin.getEmail());
            
            admin.getRoles().forEach((r) -> {
                adminUser.addRole(new Role(r));
            });

            userService.save(adminUser);

        });

    }

}
