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

import org.createnet.raptor.models.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;
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
public interface RoleRepository extends CrudRepository<Role, Long>, JpaRepository<Role, Long> {

  Role findByName(String name);
  
  @Transactional
  @Override
  public void delete(Role entity);

  @Transactional
  @Override
  public void delete(Long id);

  @Override
  public boolean exists(Long id);

  @Override
  public Role findOne(Long id);

  @Transactional
  @Override
  public <R extends Role> R save(R entity);
  
}
