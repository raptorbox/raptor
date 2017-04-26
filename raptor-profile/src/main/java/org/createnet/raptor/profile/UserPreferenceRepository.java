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
import org.createnet.raptor.models.profile.UserPreference;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public interface UserPreferenceRepository extends MongoRepository<UserPreference, String> {

    public List<UserPreference> findByUserId(String userId);
    public UserPreference findOneByUserIdAndName(String userId, String name);

}