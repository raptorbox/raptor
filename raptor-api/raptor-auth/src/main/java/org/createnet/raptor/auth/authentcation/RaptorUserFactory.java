package org.createnet.raptor.auth.authentcation;

import java.util.List;
import java.util.stream.Collectors;
import org.createnet.raptor.common.authentication.RaptorUserDetails;
import org.createnet.raptor.models.auth.Role;
import org.createnet.raptor.models.auth.User;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class RaptorUserFactory {

  private RaptorUserFactory() {
  }

  public static User create(User user) {
    return new RaptorUserDetails(user);
  }

  private static List<GrantedAuthority> mapToGrantedAuthorities(List<Role> roles) {
    return roles.stream()
            .map(role -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toList());
  }
}
