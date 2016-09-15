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
package org.createnet.raptor.auth.service.controller;

import org.createnet.raptor.auth.service.entity.Token;
import org.createnet.raptor.auth.service.entity.repository.TokenRepository;
import org.createnet.raptor.auth.service.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@RestController
public class TokenController {

  @Autowired
  private TokenRepository tokenRepository;

  @RequestMapping("/user/{uuid}/tokens")
  public Iterable<Token> getTokens(
          @AuthenticationPrincipal User user,
          @PathVariable String uuid
  ) {
    // TODO add ACL checks
    return tokenRepository.findByUserUuid(uuid);
  }

  @RequestMapping(value = "/user/{uuid}/tokens/{tid}", method = RequestMethod.GET)
  public Token get(
          @AuthenticationPrincipal User user,
          @PathVariable String uuid,
          @PathVariable Long tokenId
  ) {
    // TODO add ACL checks
    return tokenRepository.findOne(tokenId);
  }
  
  @RequestMapping(value = "/user/{uuid}/tokens/{tid}", method = RequestMethod.PUT)
  public Token update(
          @AuthenticationPrincipal User user,
          @PathVariable String uuid,
          @PathVariable Long tokenId,
          @RequestBody Token token
  ) {
    
    token.setId(tokenId);
    
    // TODO add ACL checks
    
    return tokenRepository.save(token);
  }

}
