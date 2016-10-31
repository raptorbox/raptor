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
package org.createnet.raptor.http.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Provider
public class ApiExceptionMapper implements ExceptionMapper<Exception> {

  public class JsonErrorResponse {

    public int code;
    public String message;

    public JsonErrorResponse(int code, String message) {
      this.code = code;
      this.message = message;
    }

  }

  private static final Logger logger = LoggerFactory.getLogger(ApiExceptionMapper.class);

  @Override
  public Response toResponse(Exception e) {

    if (e instanceof WebApplicationException) {
    
      logger.error("API exception: [{}] {}", e.getClass().getName(), e.getMessage());
    
      WebApplicationException ex = (WebApplicationException) e;

      int code = ex.getResponse().getStatus();
      String message = ex.getResponse().getStatusInfo().getReasonPhrase();

      return Response
              .status(code)
              .type(MediaType.APPLICATION_JSON)
              .entity(new JsonErrorResponse(code, message))
              .build();
    }

    if (e instanceof RaptorComponent.ValidationException) {
      logger.error("Validation exception: {}", e.getMessage());
      return Response
              .status(Response.Status.BAD_REQUEST)
              .entity(new JsonErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), e.getMessage()))
              .type("application/json")
              .build();
    }

    logger.error("Unhandled exception stack: {}", e.getMessage(), e);

    return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(new JsonErrorResponse(500, "Internal exception occured"))
            .type("application/json")
            .build();
  }

}
