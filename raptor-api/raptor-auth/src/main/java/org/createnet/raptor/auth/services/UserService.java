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
package org.createnet.raptor.auth.services;

import java.util.stream.Collectors;
import org.createnet.raptor.auth.exception.PasswordMissingException;
import org.createnet.raptor.auth.repository.GroupRepository;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.auth.repository.UserRepository;
import org.createnet.raptor.models.auth.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
@CacheConfig(cacheNames = "users")
public class UserService {

    final private Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    public Iterable<User> list() {
        return userRepository.findAll();
    }

    @CacheEvict(key = "#user.uuid")
    public User save(User user) {
        loadGroups(user);
        User saved = userRepository.save(user);
        //populate cache again
        return getByUuid(saved.getUuid());
    }

    @Cacheable(key = "#uuid")
    public User getByUuid(String uuid) {
        return userRepository.findByUuid(uuid);
    }

    /**
     * Load roles to ensure they are managed by hibernate
     *
     * @param user
     */
    protected void loadGroups(User user) {
        user.setGroups(
                user.getGroups()
                        .stream()
                        .map((Group g) -> {
                            return groupRepository.findByName(g.getName());
                        })
                        .filter((g) -> g != null)
                        .collect(Collectors.toList())
        );
    }

    public User update(String uuid, User rawUser) {
        User user = userRepository.findByUuid(uuid);
        return update(user, rawUser);
    }

    @CacheEvict(key = "#user.uuid")
    public User update(User user, User rawUser) {

        if (rawUser.getUsername() != null && !rawUser.getUsername().isEmpty()) {
            user.setUsername(rawUser.getUsername());
        }

        if (rawUser.getFirstname() != null && !rawUser.getFirstname().isEmpty()) {
            user.setFirstname(rawUser.getFirstname());
        }

        if (rawUser.getLastname() != null && !rawUser.getLastname().isEmpty()) {
            user.setLastname(rawUser.getLastname());
        }

        if (rawUser.getEmail() != null && !rawUser.getEmail().isEmpty()) {
            user.setEmail(rawUser.getEmail());
        }

        if (rawUser.getEnabled() != null) {
            user.setEnabled(rawUser.getEnabled());
        }

        // TODO add Group repository
        if (!rawUser.getGroups().isEmpty()) {
            rawUser.getGroups().stream().forEach(g -> user.addGroup(g));
            loadGroups(user);
        }

        if (rawUser.getPassword() != null && !rawUser.getPassword().isEmpty()) {
            String passwd = rawUser.getPassword();
            user.setPassword(encodePassword(passwd));
        }

        logger.debug("Update user data id:{}", user.getId());

        return save(user);
    }

    public User create(User rawUser) {

        String passwd = rawUser.getPassword();
        if (passwd != null && !passwd.isEmpty()) {
            rawUser.setPassword(encodePassword(passwd));
        } else {
            throw new PasswordMissingException();
        }

        loadGroups(rawUser);

        logger.debug("Create new user {}", rawUser.getUsername());
        return save(rawUser);
    }

    @CacheEvict(key = "#user.uuid")
    public void delete(User user) {
        userRepository.delete(user.getId());
    }

    protected String encodePassword(String secret) {
        return passwordEncoder.encode(secret);
    }

    public boolean exists(User rawUser) {

        User user = getByUuid(rawUser.getUuid());
        if (user != null) {
            return true;
        }

        if (rawUser.getUsername() != null && !rawUser.getUsername().isEmpty()) {
            if (findByUsername(rawUser.getUsername()) != null) {
                return true;
            }
        }

        return false;
    }

    @Transactional
    public User findByEmail(String username) {
        return userRepository.findByEmail(username);
    }

    @Transactional
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

}
