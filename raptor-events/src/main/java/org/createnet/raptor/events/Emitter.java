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
package org.createnet.raptor.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class Emitter {
  
  private final Logger logger = LoggerFactory.getLogger(Emitter.class);
  
  final private Map<String, List<Callback>> events = new HashMap();
  final private BlockingQueue<Runnable> queue = new LinkedBlockingQueue();
  final private ExecutorService executorService = new ThreadPoolExecutor(1, 10, 30, TimeUnit.SECONDS, queue);  
  
  public class EmitterException extends Exception {

    public EmitterException() {
    }

    public EmitterException(String message) {
      super(message);
    }

    public EmitterException(Throwable cause) {
      super(cause);
    }
    
  }
  
  public interface Callback {
    public void run(Event event) throws EmitterException;
  }
  
  public void on(String event, Callback callback) {
    getEvents(event).add(callback);
  }

  public void off(String event, Callback callback) {
    getEvents(event).remove(callback);
  }
  
  public void off(String event) {
    getEvents(event).clear();
  }

  public void trigger(String name, Event arg) {
    for (Iterator<Callback> iterator = getEvents(name).iterator(); iterator.hasNext();) {
      final Callback next = iterator.next();
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          try {
            arg.setEvent(name);
            next.run(arg);
          } catch (EmitterException ex) {
            logger.error("Event "+ name +" execution error", ex);
          }
        }
      });
    }
  }
  
  public boolean hasCallbacks(String name) {
    return !getEvents(name).isEmpty();
  }
  
  protected List<Callback> getEvents(String name) {
    if (events.getOrDefault(name, null) == null) {
      events.put(name, new ArrayList<>());
    }
    return events.get(name);
  }

}
