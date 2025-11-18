package org.example.service;

import org.example.entity.RoleEnum;
import org.example.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final String phoneNumber;
    private final String passwordHash;
    private final RoleEnum role;
    private final boolean accountExpired;
    private final boolean locked;
    private final boolean credentialsExpired;
    private final boolean enabled;

    public UserDetailsImpl(User user) {
        this.phoneNumber = user.getPhoneNumber();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole();
        // Загружаем значения из сущности пользователя
        this.accountExpired = user.isAccountExpired();
        this.locked = user.isLocked();
        this.credentialsExpired = user.isCredentialsExpired();
        this.enabled = user.isEnabled();
    }

    @Override
    public String getUsername() {
        return phoneNumber;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(this.role.name()));
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !this.accountExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !this.credentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public RoleEnum getRole() {
        return this.role;
    }
}
