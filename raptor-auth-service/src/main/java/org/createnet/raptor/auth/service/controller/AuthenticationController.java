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

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import org.createnet.raptor.auth.entity.LoginResponse;
import org.createnet.raptor.auth.service.entity.Token;
import org.createnet.raptor.auth.service.entity.User;
import org.createnet.raptor.auth.service.services.TokenService;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@RestController
public class AuthenticationController {

  protected static class JwtRequest {

    public JwtRequest() {}

    public JwtRequest(String username, String password) {
      this.username = username;
      this.password = password;
    }
    
    public String username;
    public String password;
  }

  protected static class JwtResponse extends LoginResponse {
    public JwtResponse(User user, String token) {
      this.user = user;
      this.token = token;
    }
    public String token;
    public User user;
  }

  @Value("${jwt.header}")
  private String tokenHeader;

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private UserDetailsService userDetailsService;
  
  @Autowired
  private TokenService tokenService;

  @RequestMapping(value = "${jwt.route.authentication.path}", method = RequestMethod.POST)
  public ResponseEntity<?> login(@RequestBody JwtRequest authenticationRequest) throws AuthenticationException {

    final Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authenticationRequest.username,authenticationRequest.password)
    );
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // Reload password post-security so we can generate token
    final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.username);
    final Token token = tokenService.createLoginToken((User)userDetails);
    
    // Return the token
    return ResponseEntity.ok(new JwtResponse((User) userDetails, token.getToken()));
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(value = "${jwt.route.authentication.refresh}", method = RequestMethod.GET)
  public ResponseEntity<?> refreshToken(
    HttpServletRequest request,
    Principal principal
  ) {

//    User user = (User) ((Authentication) principal).getPrincipal();
    String reqToken = request.getHeader(tokenHeader).replace("Bearer ", "");
    
    Token token = tokenService.read(reqToken);
    
    if(token == null) {
      return ResponseEntity.badRequest().body(null);
    }
    
    Token refreshedToken = tokenService.refreshToken(token);
    return ResponseEntity.ok(new JwtResponse((User) principal, refreshedToken.getToken()));
  }

}
