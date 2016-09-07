/*
 * Copyright 2016 CREATE-NET
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
package org.createnet.raptor.auth.service;

import org.createnet.raptor.auth.service.entity.UserRepository;
import org.createnet.raptor.auth.service.entity.User;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class RaptorUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Autowired
  public RaptorUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByLogin(username);
    if (user == null) {
      throw new UsernameNotFoundException(String.format("User %s does not exist!", username));
    }
    return new RaptorUserDetails(user);
  }

  public final static class RaptorUserDetails extends User implements UserDetails {
    
    protected User user;
    
    private static final long serialVersionUID = 1L;

    private RaptorUserDetails(User user) {
      super(user);
      this.user = user;
    }
    
    public User getUser() {
     return user;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      return getRoles();
    }

    @Override
    public String getUsername() {
      return getLogin();
    }

    @Override
    public boolean isAccountNonExpired() {
      return true;
    }

    @Override
    public boolean isAccountNonLocked() {
      return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
      return true;
    }

    @Override
    public boolean isEnabled() {
      return true;
    }

  }

}
