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
package org.createnet.raptor.auth.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="Device not found")
public class DeviceNotFoundException extends UserNotFoundException {

  public DeviceNotFoundException(String msg) {
    super(msg);
  }

  public DeviceNotFoundException() {
    super("Device not found");
  }

  public DeviceNotFoundException(String msg, Throwable t) {
    super(msg, t);
  }
  
}
