package com.ganzithon.makers.platform.domain.auth.application.dto.response;

import com.ganzithon.makers.platform.domain.user.domain.Role;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Role role
) {
}
