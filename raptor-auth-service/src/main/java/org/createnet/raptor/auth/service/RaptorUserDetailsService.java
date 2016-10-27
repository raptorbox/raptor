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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.createnet.raptor.auth.service.entity.repository.UserRepository;
import org.createnet.raptor.auth.service.entity.User;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class RaptorUserDetailsService implements UserDetailsService {

  @Autowired
  private UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username);
    if (user == null) {
      throw new UsernameNotFoundException(String.format("User %s does not exist!", username));
    }
    return new RaptorUserDetails(user);
  }

  public final static class RaptorUserDetails extends User implements UserDetails {

    private static final long serialVersionUID = 1L;

    public RaptorUserDetails(User user) {
      super(user);
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      return this.getRoles();
    }

    @Override
    public String getUsername() {
      return super.getUsername();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
      return isEnabled();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
      return isEnabled();
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
      return isEnabled();
    }

  }

}
