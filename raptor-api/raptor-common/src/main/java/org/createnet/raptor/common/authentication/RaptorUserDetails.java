/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.common.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.createnet.raptor.models.auth.Permission;
import org.createnet.raptor.models.auth.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author l
 */
public final class RaptorUserDetails extends User implements UserDetails {

    private static final long serialVersionUID = 1L;
    
    protected List<Permission> authorities = null;
    
    public RaptorUserDetails(User user) {
        super(user);
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            authorities = new ArrayList();
            this.getGroups().forEach((g) -> authorities.addAll(g.getPermissions()));
            authorities.stream().distinct().collect(Collectors.toList());
        }
        return authorities;
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
