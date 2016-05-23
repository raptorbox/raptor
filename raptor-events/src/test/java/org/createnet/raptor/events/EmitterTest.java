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

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class EmitterTest {

  private Emitter emitter;
  
  
  public EmitterTest() {
  }
  
  @BeforeClass
  public static void setUpClass() {
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() {
    emitter = new Emitter();
  }
  
  @After
  public void tearDown() {
    emitter = null;
  }

  @Test
  public void testOn() {
    
    emitter.on("assertTrue", new Emitter.Callback() {
      @Override
      public void run(Event event) throws Emitter.EmitterException {
        assertTrue(true);
      }
    });
    
    emitter.trigger("assertTrue", new AbstractEvent(){});
    
  }
  
  @Test
  public void testOff() {
    
    Emitter.Callback cb = new Emitter.Callback() {
      @Override
      public void run(Event event) throws Emitter.EmitterException {
        assertTrue(true);
      }
    };
    
    emitter.on("assertTrue", cb);
    
    emitter.trigger("assertTrue", new AbstractEvent(){});
    
    emitter.off("assertTrue", cb);

    assertFalse(emitter.hasCallbacks("assertTrue"));
    
    emitter.on("assertTrue", cb);
    emitter.off("assertTrue");
    
    assertFalse(emitter.hasCallbacks("assertTrue"));
    
  }
  
}
