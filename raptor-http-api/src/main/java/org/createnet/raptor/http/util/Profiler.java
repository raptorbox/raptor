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
package org.createnet.raptor.http.util;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class Profiler {
  
  protected static final Logger logger = LoggerFactory.getLogger(Profiler.class);

  public static Profiler instance(String id) {
    return new Profiler(id);
  }
  
  final protected Instant start;
  final protected String id;

  protected Profiler(String id) {
    start = Instant.now();
    this.id = id;
  }
  
  protected Profiler() {
    start = Instant.now();
    id = ""+ start.toEpochMilli();
  }
  
  protected Long getTime() {
    return (Instant.now().toEpochMilli() - start.toEpochMilli());
  }
  
  public void log(String message) {
    logger.debug("[{}] +{}ms - {}", id, getTime(), message);
  }
  
  public void log() {
    log("");
  }
  
}
