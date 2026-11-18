package com.ganzithon.makers.backoffice.auth.presentation;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.common.security.AuthUser;
import com.ganzithon.makers.common.springdoc.ApiErrorResponse;
import com.ganzithon.makers.platform.domain.auth.application.dto.request.LoginRequest;
import com.ganzithon.makers.platform.domain.auth.application.dto.request.RefreshRequest;
import com.ganzithon.makers.platform.domain.auth.application.dto.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Backoffice Auth", description = "백오피스 인증 API (ADMIN, SUPER_ADMIN 전용)")
public interface BackofficeAuthApi {

    @Operation(
            summary = "백오피스 로그인",
            description = "loginId와 password로 로그인하여 accessToken을 발급받습니다. ADMIN, SUPER_ADMIN만 로그인 가능합니다."
    )
    @ApiErrorResponse(value = ErrorCode.INVALID_CREDENTIALS, description = "아이디 또는 비밀번호가 올바르지 않은 경우")
    @ApiErrorResponse(value = ErrorCode.FORBIDDEN, description = "ADMIN, SUPER_ADMIN이 아닌 계정으로 로그인을 시도한 경우")
    @ApiErrorResponse(value = ErrorCode.INVALID_INPUT, description = "loginId 또는 password가 비어있는 경우")
    @PostMapping("/login")
    ResponseEntity<SuccessResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request);

    @Operation(
            summary = "토큰 재발급",
            description = "refreshToken으로 새 accessToken과 refreshToken을 발급받습니다. refreshToken이 만료되었거나 유효하지 않으면 세션이 삭제되어 자동 로그아웃됩니다."
    )
    @ApiErrorResponse(value = ErrorCode.INVALID_REFRESH_TOKEN, description = "refreshToken이 유효하지 않거나 만료된 경우 (자동 로그아웃)")
    @ApiErrorResponse(value = ErrorCode.INVALID_INPUT, description = "refreshToken이 비어있는 경우")
    @PostMapping("/refresh")
    ResponseEntity<SuccessResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshRequest request);

    @Operation(
            summary = "백오피스 로그아웃",
            description = "현재 세션을 종료합니다. 이후 동일 토큰으로는 접근할 수 없습니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiErrorResponse(value = ErrorCode.UNAUTHORIZED, description = "인증되지 않은 경우")
    @ApiErrorResponse(value = ErrorCode.TOKEN_EXPIRED, description = "accessToken이 만료된 경우 (refresh로 재발급 필요)")
    @PostMapping("/logout")
    ResponseEntity<SuccessResponse<Void>> logout(@Parameter(hidden = true) @AuthUser AuthPrincipal principal);
}
