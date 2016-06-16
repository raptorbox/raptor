/*
 * The MIT License
 *
 * Copyright 2016 CREATE-NET http://create-net.org
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

import java.util.LinkedList;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class Queue {

  LinkedList<QueueMessage> data;

  public static class QueueMessage {

    public int tries = 0;
    final public int maxRetries = 5;

    public String message;
    public String topic;

    public QueueMessage(String topic, String message) {
      this.topic = topic;
      this.message = message;
    }
    
    public boolean valid() {
      return tries <= maxRetries;
    }

  }

  public Queue() {
    data = new LinkedList<>();
  }

  synchronized public void add(QueueMessage message) {
    data.add(message);
  }

  synchronized public QueueMessage pop() {
    if(data.isEmpty()) {
      return null;
    }
    return data.pop();
  }

  synchronized public int size() {
    return data.size();
  }

  synchronized public void clear() {
    data.clear();
  }

  
}