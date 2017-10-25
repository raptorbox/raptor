/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.auth.configuration;

import java.util.List;
import java.util.Optional;
import org.createnet.raptor.auth.repository.UserRepository;
import org.createnet.raptor.auth.services.AclTokenService;
import org.createnet.raptor.auth.services.UserService;
import org.createnet.raptor.models.auth.DefaultGroup;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.configuration.RaptorConfiguration;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.RequestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@Configuration
public class DefaultAccount {

    public Logger log = LoggerFactory.getLogger(DefaultAccount.class);

    @Autowired
    private RaptorConfiguration configuration;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    AclTokenService aclTokenService;

    @Autowired
    PasswordEncoder passwordEncoder;
    
    @Bean
    ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        return new ThreadPoolTaskExecutor();
    }
    
    @EventListener(ApplicationReadyEvent.class)
    protected void createUsers() {
        threadPoolTaskExecutor().execute(() -> {
            createDefaultUser();
        });
    }
    
    protected void createDefaultUser() {

        List<org.createnet.raptor.models.configuration.AuthConfiguration.AdminUser> users = configuration.getAuth().getUsers();

        users.forEach((org.createnet.raptor.models.configuration.AuthConfiguration.AdminUser admin) -> {

            boolean createUser = true;

            User adminUser = userRepository.findByUsername(admin.getUsername());
            if (adminUser != null) {
                log.debug("User `{}` exists (id: {})", adminUser.getUsername(), adminUser.getId());

                if (admin.isLocked()) {
                    log.debug("Recreating user `{}`", adminUser.getUsername());
                    userService.delete(adminUser);
                } else {
                    createUser = false;
                }
            }

            if (createUser) {
                adminUser = new User();

                adminUser.setUsername(admin.getUsername());

                assert admin.getPassword() != null;

                adminUser.setPassword(passwordEncoder.encode(admin.getPassword()));
                adminUser.setEmail(admin.getEmail());

                for (DefaultGroup group : admin.getRoles()) {
                    adminUser.addGroup(group);
                }

                userService.save(adminUser);
                log.debug("Created user `{}` (id={})", adminUser.getUsername(), adminUser.getUuid());
            }

            if (admin.isLocked()) {

                log.debug("Registering `{}` token", admin.getUsername());

                // retry with big delay to ensure the system is up                
                RequestOptions reqOpts = RequestOptions.retriable().maxRetries(5).waitFor(5000);
                Raptor r = new Raptor(configuration.getUrl(), admin.getUsername(), admin.getPassword());
                r.Auth().login(reqOpts);

                String tokenName = "service-default";
                Optional<Token> found = r.Admin().Token().list().stream().filter((t) -> tokenName.equals(t.getName())).findFirst();

                if (!found.isPresent()) {

                    Token token = new Token();
                    token.setUser(adminUser);
                    token.setEnabled(true);
                    token.setExpires(0L);
                    token.setName(tokenName);

                    token.setSecret(admin.getPassword());
                    token.setType(Token.Type.DEFAULT);

                    token = r.Admin().Token().create(token);
                    admin.setToken(token.getToken());

                    log.debug("Created `{}` token (id={})", token.getName(), token.getId());
                }
            }

        });

    }

}
