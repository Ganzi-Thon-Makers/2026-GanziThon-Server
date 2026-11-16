package com.ganzithon.makers.common.security;

import com.ganzithon.makers.platform.domain.user.domain.Role;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record AuthUser(Long userId, Role role, String sessionId) {

    public List<GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
