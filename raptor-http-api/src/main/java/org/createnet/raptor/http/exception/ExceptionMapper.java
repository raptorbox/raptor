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
package org.createnet.raptor.http.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Throwable> {

  private static final Logger logger = LoggerFactory.getLogger(ExceptionMapper.class);

  @Override
  public Response toResponse(Throwable e) {
    
    logger.error("API Exception", e);
    
    if(e instanceof WebApplicationException) {
      return ((WebApplicationException) e).getResponse();
    }      
    
    if(e instanceof RaptorComponent.ValidationException) {
      return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity("{ \"code\": 500, \"reason\": \""+ e.getMessage() +"\"}")
            .type("application/json")
            .build();
    }   

    return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity("{ \"code\": 500, \"reason\": \"Internal exception occured\"}")
            .type("application/json")
            .build();
  }

}
