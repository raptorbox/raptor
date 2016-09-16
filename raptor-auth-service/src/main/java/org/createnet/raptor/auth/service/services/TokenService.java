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

import org.createnet.raptor.auth.service.entity.Role;
import org.createnet.raptor.auth.service.entity.Token;
import org.createnet.raptor.auth.service.entity.User;
import org.createnet.raptor.auth.service.entity.repository.RoleRepository;
import org.createnet.raptor.auth.service.entity.repository.TokenRepository;
import org.createnet.raptor.auth.service.jwt.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class TokenService {

  private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

  @Autowired
  private JwtTokenUtil tokenUtil;

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

  /** 
   * @deprecated this should be dropped in favour of forcing to have a secret 
   * provided each time or preset at user options level
   */ 
  public Token create(String name, User user) {
    return create(name, user, null);
  }
  
  public Token create(String name, User user, String secret) {

    Token token = new Token();

    token.setUser(user);
    token.setToken(tokenUtil.generateToken(user, secret));
    token.setName(name);

    tokenRepository.save(token);
    return token;
  }

}
