package com.caprock.security.services;

import com.caprock.model.User;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String role;

    //We build this from our User entity
    public UserDetailsImpl(User user){
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.role = "ROLE_USER";
    }

    //Used when creating an admin - no DB user exists
    public UserDetailsImpl(Long id, String email, String password, String role){
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Long getId(){
        return id;
    }

    public String getRole(){
        return role;
    }



    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }


    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
