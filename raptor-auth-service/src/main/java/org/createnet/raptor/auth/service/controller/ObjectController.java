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

import org.createnet.raptor.auth.service.objects.CheckRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@RestController
@PreAuthorize("isAuthenticated()")
public class ObjectController {

  private static final Logger logger = LoggerFactory.getLogger(ObjectController.class);

  @RequestMapping(value = "/check", method = RequestMethod.POST)
  public ResponseEntity<String> checkPermission(
    @RequestBody CheckRequestBody body
  ) {
  
    body.validate();

    return ResponseEntity.status(HttpStatus.CONTINUE).body("");
  }
  
  @RequestMapping(value = "/sync", method = RequestMethod.POST)
  public ResponseEntity<String> syncObject() {
    
    return ResponseEntity.status(HttpStatus.ACCEPTED).body("");
  }

}
