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
package org.createnet.raptor.auth.service.sec;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class JsonUsernamePasswordFilter extends UsernamePasswordAuthenticationFilter {

  public final static ObjectMapper jacksonObjectMapper = new ObjectMapper();

  protected class LoginForm {

    public String username;
    public String password;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
          HttpServletResponse response) throws AuthenticationException {

    if (!request.getMethod().equals("POST")) {
      throw new AuthenticationServiceException(
              "Authentication method not supported: " + request.getMethod());
    }

    if (!request.getContentType().equals(MediaType.APPLICATION_JSON)) {
      throw new AuthenticationServiceException(
              "Only Content-Type " + MediaType.APPLICATION_JSON + " is supported. Provided is " + request.getContentType());
    }

    LoginForm loginForm;
    try {
      InputStream body = request.getInputStream();
      loginForm = jacksonObjectMapper.readValue(body, LoginForm.class);
    } catch (IOException ex) {
      throw new AuthenticationServiceException("Error reading body", ex);
    }

    if (loginForm.username == null) {
      loginForm.username = "";
    }

    if (loginForm.password == null) {
      loginForm.password = "";
    }

    loginForm.username = loginForm.username.trim();

    UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(loginForm.username, loginForm.password);
    setDetails(request, authRequest);

    return this.getAuthenticationManager().authenticate(authRequest);
  }

  @Override
  protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
    authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
  }

}
