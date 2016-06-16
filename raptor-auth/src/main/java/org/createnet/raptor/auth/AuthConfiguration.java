/*
 * Copyright 2016 CREATE-NET http://create-net.org
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
package org.createnet.raptor.auth;

import org.createnet.raptor.config.Configuration;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class AuthConfiguration implements Configuration {
  
  public enum Type {
    token, allow_all
  }
  
  public enum Cache {
    memory, none
  }
  
  public String type;
  public String cache;
  public Token token = new Token();
  
  static public class Token {
    
    static public class Truststore {
      public String path;
      public String password;
    }
    
    public Truststore truststore = new Truststore();
    public String checkUrl;
    public String syncUrl;
  }
  
}
