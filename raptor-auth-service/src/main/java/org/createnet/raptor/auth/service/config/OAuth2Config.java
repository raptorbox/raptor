/*
 * Copyright 2016 CREATE-NET.
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
package org.createnet.raptor.auth.service.config;

import org.createnet.raptor.auth.service.handler.RaptorUserApprovalHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.TokenApprovalStore;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@Configuration
@EnableAuthorizationServer
@EnableWebSecurity
public class OAuth2Config extends AuthorizationServerConfigurerAdapter {

  private static final String DUMMY_RESOURCE_ID = "dummy";

  @Autowired
  @Qualifier("authenticationManagerBean")
  private AuthenticationManager authenticationManager;

  @Value("${default.redirect:http://localhost:8080/tonr2/sparklr/redirect}")
  private String defaultRedirectUri;

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    endpoints.tokenStore(tokenStore()).tokenEnhancer(jwtTokenEnhancer()).authenticationManager(authenticationManager);
  }

  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(jwtTokenEnhancer());
  }

  @Bean
  protected JwtAccessTokenConverter jwtTokenEnhancer() {
    KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource("jwt.jks"), "testpass".toCharArray());
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    converter.setKeyPair(keyStoreKeyFactory.getKeyPair("jwt-test"));
    return converter;
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.inMemory().withClient("tonr")
            .resourceIds(DUMMY_RESOURCE_ID)
            .authorizedGrantTypes("authorization_code", "implicit")
            .authorities("ROLE_CLIENT")
            .scopes("read", "write")
            .secret("secret")
            .and()
            .withClient("tonr-with-redirect")
            .resourceIds(DUMMY_RESOURCE_ID)
            .authorizedGrantTypes("authorization_code", "implicit")
            .authorities("ROLE_CLIENT")
            .scopes("read", "write")
            .secret("secret")
            .redirectUris(defaultRedirectUri)
            .and()
            .withClient("my-client-with-registered-redirect")
            .resourceIds(DUMMY_RESOURCE_ID)
            .authorizedGrantTypes("authorization_code", "client_credentials")
            .authorities("ROLE_CLIENT")
            .scopes("read", "trust")
            .redirectUris("http://anywhere?key=value")
            .and()
            .withClient("my-trusted-client")
            .authorizedGrantTypes("password", "authorization_code", "refresh_token", "implicit")
            .authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT")
            .scopes("read", "write", "trust")
            .accessTokenValiditySeconds(60)
            .and()
            .withClient("my-trusted-client-with-secret")
            .authorizedGrantTypes("password", "authorization_code", "refresh_token", "implicit", "client_credentials")
            .authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT")
            .scopes("read", "write", "trust")
            .secret("somesecret")
            .and()
            .withClient("my-less-trusted-client")
            .authorizedGrantTypes("authorization_code", "implicit")
            .authorities("ROLE_CLIENT")
            .scopes("read", "write", "trust")
            .and()
            .withClient("my-less-trusted-autoapprove-client")
            .authorizedGrantTypes("implicit")
            .authorities("ROLE_CLIENT")
            .scopes("read", "write", "trust")
            .autoApprove(true);
  }

  @Override
  public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
    oauthServer.realm("sparklr2/client");
  }

  protected static class Stuff {

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private TokenStore tokenStore;

    @Bean
    public ApprovalStore approvalStore() throws Exception {
      TokenApprovalStore store = new TokenApprovalStore();
      store.setTokenStore(tokenStore);
      return store;
    }

    @Bean
    @Lazy
    @Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
    public RaptorUserApprovalHandler userApprovalHandler() throws Exception {
      RaptorUserApprovalHandler handler = new RaptorUserApprovalHandler();
      handler.setApprovalStore(approvalStore());
      handler.setRequestFactory(new DefaultOAuth2RequestFactory(clientDetailsService));
      handler.setClientDetailsService(clientDetailsService);
      handler.setUseApprovalStore(true);
      return handler;
    }
  }
}
