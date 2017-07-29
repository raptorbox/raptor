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
package org.createnet.raptor.tree;

import com.querydsl.core.types.Predicate;
import java.util.List;
import org.createnet.raptor.models.tree.TreeNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Repository
public interface TreeRepository extends MongoRepository<TreeNode, String>, QueryDslPredicateExecutor<TreeNode> {
    
    public TreeNode findByParentId(String deviceId);

    @Override
    public Iterable<TreeNode> findAll(Iterable<String> ids);

    @Override
    public <S extends TreeNode> List<S> save(Iterable<S> entites);

    @Override
    public void delete(String id);

    @Override
    public TreeNode findOne(String id);

    @Override
    public <S extends TreeNode> S save(S entity);

    @Override
    public Page<TreeNode> findAll(Predicate predicate, Pageable pageable);
    
    
    @Override
    public List<TreeNode> findAll(Predicate prdct);    
    
}
