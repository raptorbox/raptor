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
package org.createnet.raptor.application;

import java.util.List;
import org.createnet.raptor.models.app.App;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Repository
@Component
public interface AppRepository extends CrudRepository<App, String>, QueryDslPredicateExecutor<App> {

    public List<App> findByUserId(String userId);
    public App findOneById(String id);
    public App findOneByUserIdAndName(String userId, String name);
    
}
