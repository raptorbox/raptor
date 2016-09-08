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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.service.spi.ServiceException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@ControllerAdvice
public class RaptorExceptionHandler extends ResponseEntityExceptionHandler {

  class ErrorResponseBody {

    public ErrorResponseBody() {}
    
    public ErrorResponseBody(HttpStatus code) {
      this.code = code;
      this.message = code.getReasonPhrase();
    }
    
    public HttpStatus code = HttpStatus.INTERNAL_SERVER_ERROR;
    public String message = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
  }

  @ExceptionHandler(Throwable.class)
  @ResponseBody
  ResponseEntity<ErrorResponseBody> handleControllerException(HttpServletRequest req, Throwable ex) {
    
    ErrorResponseBody response;
    
    if(ex instanceof AccessDeniedException) {
      response = new ErrorResponseBody(HttpStatus.BAD_REQUEST);
    }
    else {
      response = new ErrorResponseBody();
    }
    
    return new ResponseEntity(response, response.code);
  }

}
