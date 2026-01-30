package com.gringotts.transaction.transaction_service.security.jwt;

import com.gringotts.transaction.transaction_service.security.iam.User;
import com.gringotts.transaction.transaction_service.security.iam.IsActive;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomerUserDetails implements UserDetails {

    private final Long userId;
    private final String phoneNumber;
    private final String passwordHash;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomerUserDetails(User user) {
        this.userId = user.getUserId();
        this.phoneNumber = user.getPhoneNumber();
        this.passwordHash = user.getPasswordHash();
        this.enabled = user.getIsActive().equals(IsActive.ACTIVE);
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole()));
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return phoneNumber;
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
        return enabled;
    }
}
