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
package org.createnet.raptor.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;
import org.createnet.raptor.models.auth.Role;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.auth.service.repository.UserRepository;
import org.createnet.raptor.auth.service.services.BrokerMessageHandler;
import org.createnet.raptor.auth.service.services.UserService;
import org.createnet.raptor.config.ConfigurationLoader;
import org.createnet.raptor.dispatcher.DispatcherConfiguration;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
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
@EntityScan(basePackageClasses = org.createnet.raptor.models.auth.User.class)
@EnableScheduling
public class Application {

    static final public ObjectMapper mapper = new ObjectMapper();

    static ConfigurableApplicationContext instance;

    public static void main(String[] args) {
        getInstance();
    }

    static public ConfigurableApplicationContext getInstance() {
        return getInstance(new String[]{});
    }

    static public ConfigurableApplicationContext getInstance(String[] args) {
        if (instance == null) {
            instance = new SpringApplicationBuilder(Application.class)
                    .bannerMode(Banner.Mode.OFF)
                    .logStartupInfo(false)
                    .headless(true)
                    .web(true)
                    //            .initializers(new YamlFileApplicationContextInitializer())
                    .application()
                    .run(args);
        }
        return instance;
    }

    static public void close() {
        getInstance().close();
    }

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
    private BrokerMessageHandler messageHandler;

    @Autowired
    private UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
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
            //userRepository.delete(defaultUser.getId());
            return;
        }

        User adminUser = new User();

        adminUser.setUsername(defaultUserUsername);
        adminUser.setPassword(passwordEncoder().encode(defaultUserPassword));
        adminUser.setEmail(defaultUserEmail);

//    adminUser.addRole(roleRepository.findByName(Role.Roles.ROLE_SUPER_ADMIN.name()));
        adminUser.addRole(Role.Roles.super_admin);

        userService.save(adminUser);

    }

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {

        DispatcherConfiguration config = (DispatcherConfiguration) ConfigurationLoader.getConfiguration("dispatcher", DispatcherConfiguration.class);

        DefaultMqttPahoClientFactory f = new DefaultMqttPahoClientFactory();

        f.setUserName(config.username);
        f.setPassword(config.password);
        f.setServerURIs(config.uri);
        f.setCleanSession(true);
        f.setPersistence(new MemoryPersistence());

        return f;
    }

    // Add inbound MQTT support
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter("raptorauth", mqttClientFactory(), "+/events");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(0);
        adapter.setRecoveryInterval(1000);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return (Message<?> message) -> {
            messageHandler.handle(message);
        };
    }

//    // Spring outbound channel
//    @Bean
//    @ServiceActivator(inputChannel = "mqttOutboundChannel")
//    public MessageHandler mqttOutbound() {
//        MqttPahoMessageHandler messageHandler =
//                       new MqttPahoMessageHandler("authclient", mqttClientFactory());
//        messageHandler.setAsync(true);
//        messageHandler.setDefaultTopic("baz");
//        return messageHandler;
//    }
//
//    @Bean
//    public MessageChannel mqttOutboundChannel() {
//        return new DirectChannel();
//    }
//
//    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
//    public interface MyGateway {
//        void sendToMqtt(String data);
//    }
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
