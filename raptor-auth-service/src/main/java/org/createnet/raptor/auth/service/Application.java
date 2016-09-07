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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

@SpringBootApplication
@Configuration
@EnableAutoConfiguration
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Autowired
  private DataSource dataSource;

  @Autowired
  public void init(AuthenticationManagerBuilder auth) throws Exception {
    auth
            .jdbcAuthentication().dataSource(dataSource)
            .withUser("admin")
            .password("admin").roles("ADMIN");
  }

  @Configuration
  @EnableResourceServer
  protected static class ResourceServer extends ResourceServerConfigurerAdapter {

    @Autowired
    private TokenStore tokenStore;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources)
            throws Exception {
      resources.tokenStore(tokenStore);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
      http.authorizeRequests().anyRequest().authenticated();
    }

  }

  @Configuration
  @EnableAuthorizationServer
  public class OAuth2ServerConfiguration extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager auth;

    @Autowired
    private DataSource dataSource;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Bean
    public JdbcTokenStore tokenStore() {
      return new JdbcTokenStore(dataSource);
    }

    @Bean
    protected AuthorizationCodeServices authorizationCodeServices() {
      return new JdbcAuthorizationCodeServices(dataSource);
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security)
            throws Exception {
      security.passwordEncoder(passwordEncoder);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints)
            throws Exception {
      endpoints.authorizationCodeServices(authorizationCodeServices())
              .authenticationManager(auth).tokenStore(tokenStore())
              .approvalStoreDisabled();
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
      // @formatter:off
      clients.jdbc(dataSource)
              .passwordEncoder(passwordEncoder)
              .withClient("my-trusted-client")
              .authorizedGrantTypes("password", "authorization_code",
                      "refresh_token", "implicit")
              .authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT")
              .scopes("read", "write", "trust")
              .resourceIds("oauth2-resource")
              .accessTokenValiditySeconds(60).and()
              .withClient("my-client-with-registered-redirect")
              .authorizedGrantTypes("authorization_code")
              .authorities("ROLE_CLIENT").scopes("read", "trust")
              .resourceIds("oauth2-resource")
              .redirectUris("http://anywhere?key=value").and()
              .withClient("my-client-with-secret")
              .authorizedGrantTypes("client_credentials", "password")
              .authorities("ROLE_CLIENT").scopes("read")
              .resourceIds("oauth2-resource").secret("secret");
      // @formatter:on
    }

  }

}
