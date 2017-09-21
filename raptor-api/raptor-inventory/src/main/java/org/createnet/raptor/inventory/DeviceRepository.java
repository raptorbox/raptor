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
package org.createnet.raptor.inventory;

import java.util.List;
import com.querydsl.core.types.Predicate;
import org.createnet.raptor.models.objects.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Repository
@Component
public interface DeviceRepository extends MongoRepository<Device, String>, QueryDslPredicateExecutor<Device> {

    public List<Device> findByUserId(String userId);
    public List<Device> findByNameAndUserId(String name, String userId);
    
    @Override
    public List<Device> findAll();
    
    @Override
    public Page<Device> findAll(Predicate predicate, Pageable pageable);

    @Override
    public List<Device> findAll(Predicate prdct);
    

}
