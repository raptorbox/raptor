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
package org.createnet.raptor.data;

import com.querydsl.core.types.Predicate;
import java.util.List;
import org.createnet.raptor.models.data.RecordSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public interface StreamRepository extends MongoRepository<RecordSet, String>, QueryDslPredicateExecutor<RecordSet> {
    
    public List<RecordSet> findByDeviceIdAndStreamId(String deviceId, String streamId, Pageable page);
    public Page<RecordSet> findOneByDeviceIdAndStreamId(String deviceId, String streamId, Pageable page);
    public void deleteByDeviceIdAndStreamId(String deviceId, String streamId);
    
    @Override
    public void deleteAll();
 
    @Override
    public Page<RecordSet> findAll(Predicate predicate, Pageable pageable);

    @Override
    public List<RecordSet> findAll(Predicate prdct);    
    
}
