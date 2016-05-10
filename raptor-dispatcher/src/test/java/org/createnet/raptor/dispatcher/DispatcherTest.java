/*
 * The MIT License
 *
 * Copyright 2016 Luca Capra <lcapra@create-net.org>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.createnet.raptor.dispatcher;

import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class DispatcherTest {
  
  Map<String, String> config;
  
  public DispatcherTest() {
    Map<String, String> config = new HashMap();
    config.put("uri", "tcp://serviotciy.local:1883");
    config.put("username", "compose");
    config.put("password", "shines");
    
  }
  
  @BeforeClass
  public static void setUpClass() {
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() {
  }

  /**
   * Test of add method, of class Dispatcher.
   */
//  @Test
//  public void testAdd() {
//    String topic = "test/topic";
//    String message = "message";
//    Dispatcher instance = new Dispatcher(this.config);
//    instance.add(topic, message);
//    assertEquals(instance.size(), 0);
//    
//  }
  
  
//  /**
//   * Test huge add
//   */
//  @Test
//  public void testAddMany() throws InterruptedException {
//    
//    String topic = "test/topic";
//    String message = "message";
//    
//    ExecutorService exec = Executors.newCachedThreadPool();
//
//    Dispatcher instance = new Dispatcher(this.config);
//    
//    int i = 0;
//    while(i < 10) {
//      exec.submit(() -> {
//        instance.add(topic, message);
//      });
//      i++;
//    }
//    
//    exec.awaitTermination(60, TimeUnit.SECONDS);
//    assertEquals(instance.size(), 0);
//    
//  }

  /**
   * Test of dispatch method, of class Dispatcher.
   */
//  @Test
//  public void testDispatch() {
//    Dispatcher instance = new Dispatcher(this.config);
//    instance.dispatch();
//  }

  /**
   * Test of close method, of class Dispatcher.
   */
//  @Test
//  public void testClose() {
//    Dispatcher instance = new Dispatcher(this.config);
//    instance.close();
//  }
  
}
