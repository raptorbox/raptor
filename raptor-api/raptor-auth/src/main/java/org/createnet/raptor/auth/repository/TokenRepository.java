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
package org.createnet.raptor.auth.repository;

import java.util.List;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Component
@Repository
public interface TokenRepository extends CrudRepository<Token, String>, MongoRepository<Token, String> {

    Token findByToken(String token);
    List<Token> findByUserId(String userId);
    List<Token> findByUserUuid(String userUuid);
    List<Token> findByType(Token.Type type);
    List<Token> findByTypeAndUser(Token.Type type, User user);

    @Override
    @Transactional
    Token findOne(String id);

    @Override
    @Transactional
    public void delete(Token token);

    @Override
    @Transactional
    public void delete(String id);
    
    @Override
    @Transactional
    public <S extends Token> S save(S token);

    @Override
    @Transactional
    public void delete(Iterable<? extends Token> tokens);
    
}
