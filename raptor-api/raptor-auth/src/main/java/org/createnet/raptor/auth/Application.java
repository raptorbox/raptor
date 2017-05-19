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
package org.createnet.raptor.auth;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.createnet.raptor.api.common.BaseApplication;
import org.createnet.raptor.api.common.authentication.TokenHelper;
import org.createnet.raptor.api.common.configuration.RaptorConfiguration;
import org.createnet.raptor.models.auth.Role;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.auth.repository.UserRepository;
import org.createnet.raptor.auth.services.AuthMessageHandler;
import org.createnet.raptor.auth.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.core.MessageProducer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Profile("default")
@SpringBootApplication(
        scanBasePackages = {"org.createnet.raptor.api.common", "org.createnet.raptor.auth"},
        exclude = {EmbeddedMongoAutoConfiguration.class, MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@EnableCaching
@EnableRetry
@EntityScan(basePackages = "org.createnet.raptor.models.auth")
@EnableJpaRepositories(
        basePackages = "org.createnet.raptor.auth.repository",
        entityManagerFactoryRef = "entityManagerFactory"
)
@EnableScheduling
public class Application extends BaseApplication {

    public static void main(String[] args) {
        start(Application.class, args);
    }

    @Primary
    @Bean(name = "dataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("dataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("org.createnet.raptor.models.auth")
//                .persistenceUnit("foo")
                .build();
    }
    
    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }    
    
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

    @Bean
    public MessageProducer mqttClient() {
        return createMqttClient(authMessageHandler());
    }

    @Autowired
    public void authenticationManagerBuilder(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource);
        createDefaultUser();
    }

    protected void createDefaultUser() {

        RaptorConfiguration.Auth.Admin admin = configuration.getAuth().getAdmin();

        if (!admin.isEnabled()) {
            return;
        }

        User defaultUser = userRepository.findByUsername(admin.getUsername());
        if (defaultUser != null) {
            log.debug("Default admin user `{}` exists (id: {})", defaultUser.getUsername(), defaultUser.getId());
            //userRepository.delete(defaultUser.getId());
            return;
        }

        User adminUser = new User();

        adminUser.setUsername(admin.getUsername());
        adminUser.setPassword(passwordEncoder().encode(admin.getPassword()));
        adminUser.setEmail(admin.getEmail());
        adminUser.addRole(Role.Roles.super_admin);

        userService.save(adminUser);
    }

}
