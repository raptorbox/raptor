/*
 * Copyright 2016 Luca Capra <lcapra@create-net.org>.
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
package org.createnet.raptor.http.filter;

import java.io.IOException;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.http.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */

public class AuthorizationRequestFilter implements ContainerRequestFilter {

  final private Logger logger = LoggerFactory.getLogger(AuthorizationRequestFilter.class);

  @Inject
  AuthService auth;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

    String accessToken = requestContext.getHeaderString("Authorization");
    
    boolean validToken = false;
    try {
      if (auth.validToken(accessToken)) {
        validToken = true;
      }
    } catch (Authentication.AutenticationException ex) {
      logger.error("Token is not valid", ex);
    } finally {

      if (!validToken) {
        requestContext
                .abortWith(Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Access denied")
                        .build());

      }
    }

  }
}
