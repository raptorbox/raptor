/*
 * Copyright 2017 FBK/CREATE-NET
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
package org.createnet.raptor.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import org.createnet.raptor.models.auth.User;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class LoginResponse {

//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class Role {
//        public long id;
//        public String name;
//
//        public Role(String name) {
//            this.name = name;
//        }
//
//        public Role() {
//        }
//        
//    }
//
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class User {
//
//        public String uuid;
//        public String username;
//        public List<Role> roles = new ArrayList();
//        public String firstname;
//        public String lastname;
//        public String email;
//        public String enabled;
//        long created;
//    }

    public String token = null;
    public User user = new User();

}
