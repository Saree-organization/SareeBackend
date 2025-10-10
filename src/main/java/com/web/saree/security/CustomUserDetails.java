package com.web.saree.security;

import com.web.saree.entity.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Users user;

    public CustomUserDetails(Users user) {
        this.user = user;
    }

    // ✨ FIX: Return user's role as a GrantedAuthority
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = user.getRole();

        // Ensure the role is prefixed with "ROLE_" for Spring Security checks
        if (roleName != null && !roleName.trim().isEmpty()) {
            return List.of(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()));
        }
        // Fallback for safety, though the entity defaults to "USER"
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return ""; // OTP-based system
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    public Users getUser() {
        return user;
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