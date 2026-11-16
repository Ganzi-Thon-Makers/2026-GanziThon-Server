package com.ganzithon.makers.common.security;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class SecurityPaths {

    // 로그인/토큰 재발급 (인증 불필요)
    public static final String LOGIN = "/api/v1/auth/login";
    public static final String REFRESH = "/api/v1/auth/refresh";
    public static final String BACKOFFICE_LOGIN = "/backoffice/api/v1/auth/login";
    public static final String BACKOFFICE_REFRESH = "/backoffice/api/v1/auth/refresh";

    // 어드민 전체
    public static final String BACKOFFICE_ALL = "/backoffice/api/v1/**";

    // SUPER_ADMIN 전용
    public static final String[] SUPER_ADMIN_ONLY = {
            "/backoffice/api/v1/vote/**",
            "/backoffice/api/v1/timetables/**"
    };

    // 팀 계정 전용 (스탬프 발급용 QR 생성)
    public static final String[] TEAM_ONLY = {
            "/api/v1/stamps/qr/**"
    };
}
