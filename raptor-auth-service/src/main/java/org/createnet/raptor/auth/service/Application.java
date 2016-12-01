/*
 * Copyright 2016 CREATE-NET
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
package org.createnet.raptor.auth.service;

import javax.sql.DataSource;
import org.createnet.raptor.auth.service.entity.Role;
import org.createnet.raptor.auth.service.entity.User;
import org.createnet.raptor.auth.service.entity.repository.UserRepository;
import org.createnet.raptor.auth.service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Profile("default")
@SpringBootApplication
@WebAppConfiguration
@EnableAutoConfiguration
@ComponentScan
@Configuration
@EnableCaching
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableRetry
public class Application {

    public static void main(String[] args) {
        
        
        ConfigurableApplicationContext app = new SpringApplicationBuilder(Application.class)
            .bannerMode(Banner.Mode.OFF)
            .logStartupInfo(false)
            .headless(true)
            .web(true)                
//            .initializers(new YamlFileApplicationContextInitializer())
            .application()
            .run(args);
        
    }

    static final protected BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${raptor.admin.enabled}")
    private Boolean defaultUserEnabled;
    @Value("${raptor.admin.username}")
    private String defaultUserUsername;
    @Value("${raptor.admin.password}")
    private String defaultUserPassword;
    @Value("${raptor.admin.email}")
    private String defaultUserEmail;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

//  @Autowired
//  private RoleRepository roleRepository;
    @Autowired
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource);
        createDefaultUser();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .maxAge(3600);
            }
        };
    }    
    
    protected void createDefaultUser() {

        if (defaultUserEnabled != null && defaultUserEnabled == false) {
            return;
        }

        User defaultUser = userRepository.findByUsername(defaultUserUsername);
        if (defaultUser != null) {
            userRepository.delete(defaultUser.getId());
        }

        User adminUser = new User();

        adminUser.setUsername(defaultUserUsername);
        adminUser.setPassword(passwordEncoder.encode(defaultUserPassword));
        adminUser.setEmail(defaultUserEmail);

//    adminUser.addRole(roleRepository.findByName(Role.Roles.ROLE_SUPER_ADMIN.name()));
        adminUser.addRole(Role.Roles.super_admin);

        userService.save(adminUser);

    }

//  @Configuration
//  @EnableResourceServer
//  protected static class ResourceServer extends ResourceServerConfigurerAdapter {
//
//    @Autowired
//    private TokenStore tokenStore;
//
//    @Override
//    public void configure(ResourceServerSecurityConfigurer resources)
//            throws Exception {
//      resources.tokenStore(tokenStore);
//    }
//
//    @Override
//    public void configure(HttpSecurity http) throws Exception {
//      http.authorizeRequests().anyRequest().authenticated();
//    }
//
//  }
//  @Configuration
//  @EnableAuthorizationServer
//  public class OAuth2ServerConfiguration extends AuthorizationServerConfigurerAdapter {
//
//    @Autowired
//    private AuthenticationManager auth;
//
//    @Autowired
//    private DataSource dataSource;
//
//    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//
//    @Bean
//    public JdbcTokenStore tokenStore() {
//      return new JdbcTokenStore(dataSource);
//    }
//
//    @Bean
//    protected AuthorizationCodeServices authorizationCodeServices() {
//      return new JdbcAuthorizationCodeServices(dataSource);
//    }
//
//    @Override
//    public void configure(AuthorizationServerSecurityConfigurer security)
//            throws Exception {
//      security.passwordEncoder(passwordEncoder);
//    }
//
//    @Override
//    public void configure(AuthorizationServerEndpointsConfigurer endpoints)
//            throws Exception {
//      endpoints.authorizationCodeServices(authorizationCodeServices())
//              .authenticationManager(auth).tokenStore(tokenStore())
//              .approvalStoreDisabled();
//    }
//
//    @Override
//    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
//      // @formatter:off
//      clients.jdbc(dataSource)
//              .passwordEncoder(passwordEncoder)
//              .withClient("my-trusted-client")
//              .authorizedGrantTypes("password", "authorization_code",
//                      "refresh_token", "implicit")
//              .authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT")
//              .scopes("read", "write", "trust")
//              .resourceIds("oauth2-resource")
//              .accessTokenValiditySeconds(60).and()
//              .withClient("my-client-with-registered-redirect")
//              .authorizedGrantTypes("authorization_code")
//              .authorities("ROLE_CLIENT").scopes("read", "trust")
//              .resourceIds("oauth2-resource")
//              .redirectUris("http://anywhere?key=value").and()
//              .withClient("my-client-with-secret")
//              .authorizedGrantTypes("client_credentials", "password")
//              .authorities("ROLE_CLIENT").scopes("read")
//              .resourceIds("oauth2-resource").secret("secret");
//      // @formatter:on
//    }
//
//  }
}
