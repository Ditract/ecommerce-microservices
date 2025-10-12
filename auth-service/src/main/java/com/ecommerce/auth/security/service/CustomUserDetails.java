package com.ecommerce.auth.security.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {
    private final String email;
    private final Long id;
    private final String firstName;
    private final String lastName;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean isActive;

    public CustomUserDetails(String email, Long id, String firstName, String lastName,
                             Collection<? extends GrantedAuthority> authorities, boolean isActive) {
        this.email = email;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.authorities = authorities;
        this.isActive = isActive;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // No se necesita password para JWT
    }

    @Override
    public String getUsername() {
        return email;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
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
        return isActive;
    }
}