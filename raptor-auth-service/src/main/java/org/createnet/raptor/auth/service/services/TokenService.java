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
package org.createnet.raptor.auth.service.services;

import java.time.Instant;
import org.createnet.raptor.auth.service.entity.Token;
import org.createnet.raptor.auth.service.entity.User;
import org.createnet.raptor.auth.service.entity.repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class TokenService {

  private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

  @Value("${jwt.secret}")
  private String secret;
  
  @Value("${jwt.expiration}")
  private Long expiration;  
  
  @Autowired
  private JwtTokenService tokenUtil;

  @Autowired
  private TokenRepository tokenRepository;

  public Iterable<Token> list(String uuid) {
    return tokenRepository.findByUserUuid(uuid);
  }

  public Token read(Long tokenId) {
    return tokenRepository.findOne(tokenId);
  }

  public Token update(Token rawToken) {

    Token token = read(rawToken.getId());
    if (token == null) {
      return null;
    }

    token.setName(rawToken.getName());

    tokenRepository.save(token);
    return token;
  }
  
  public Token create(Token token) {
    tokenRepository.save(token);
    return token;
  }

  public Token read(String authToken) {
    if(authToken == null) return null;
    return tokenRepository.findByToken(authToken);
  }

  public Token createLoginToken(User user) {
    Token token = tokenUtil.createToken("login", user, this.expiration, this.secret);
    token.setSecret(null);
    token.setType(Token.Type.LOGIN);
    return create(token);
  }
  
  public Token refreshToken(Token token) {
    tokenUtil.refreshToken(token);
    return tokenRepository.save(token);
  }

  public boolean isValid(Token token, String secret) {
    // Cannot read the token claims?
    if(tokenUtil.getClaims(token, secret) == null) 
      return false;
    return token.isValid();
  }
  
  public boolean isValid(Token token) {
    return isValid(token, token.getSecret() == null ? this.secret : token.getSecret());
  }

}
