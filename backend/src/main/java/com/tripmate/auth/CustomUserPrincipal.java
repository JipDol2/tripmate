package com.tripmate.auth;

import com.tripmate.user.User;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserPrincipal implements UserDetails {
    private final Long userId;
    private final String email;
    private final String nickname;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserPrincipal(Long userId, String email, String nickname,
                               Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.authorities = authorities;
    }

    public static CustomUserPrincipal from(User user) {
        return new CustomUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                List.of()
        );
    }

    public Long getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return email;
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
