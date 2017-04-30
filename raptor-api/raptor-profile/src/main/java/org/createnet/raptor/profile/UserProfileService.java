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
package org.createnet.raptor.profile;

import java.util.List;
import org.createnet.raptor.models.profile.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class UserProfileService {
    
    @Autowired
    private UserProfileRepository repository;
    
    public void save(UserProfile pref) {
        repository.save(pref);
    }
    
    public UserProfile get(String userId, String name) {
        return repository.findOneByUserIdAndName(userId, name);
    }
    
    public List<UserProfile> list(String userId) {
        return repository.findByUserId(userId);
    }

    public void delete(UserProfile pref) {
        repository.delete(pref.getId());
    }
    
}
