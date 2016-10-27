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
package org.createnet.raptor.auth;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class AuthConfiguration {

    public String type;
    public String cache;
    final public Token token = new Token();
    final public EHCache ehcache = new EHCache();

    public enum Type {
        token, allow_all
    }

    public enum Cache {
        memory, none, ehcache
    }

    static public class Token {

        static public class Truststore {

            public String path;
            public String password;
        }

        final public Truststore truststore = new Truststore();
        public String checkUrl;
        public String syncUrl;
        public String loginUrl;
    }

    static public class EHCache {

        static public class Authorization {

            public int duration = 10;
            public int heapSize = 500;
            public int inMemorySize = 10;
        }

        static public class Authentication {

            public int duration = 20;
            public int heapSize = 200;
            public int inMemorySize = 10;
        }

        final public Authorization authorization = new Authorization();
        final public Authentication authentication = new Authentication();
    }

}
