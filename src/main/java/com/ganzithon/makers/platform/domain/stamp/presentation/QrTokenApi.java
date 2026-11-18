package com.ganzithon.makers.platform.domain.stamp.presentation;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.common.security.AuthUser;
import com.ganzithon.makers.common.springdoc.ApiErrorResponse;
import com.ganzithon.makers.platform.domain.stamp.presentation.dto.QrTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = "Stamp", description = "부스 스탬프 / QR 토큰")
public interface QrTokenApi {

  @Operation(
      summary = "부스 QR 토큰 발급",
      description = "팀 계정이 자신의 부스에 표시할 QR 토큰을 발급합니다. 토큰은 일정 시간 후 만료되며, 만료 시 재발급이 필요합니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiErrorResponse(value = ErrorCode.UNAUTHORIZED, description = "인증되지 않은 경우")
  @ApiErrorResponse(value = ErrorCode.TOKEN_EXPIRED, description = "accessToken이 만료된 경우 (refresh로 재발급 필요)")
  @ApiErrorResponse(value = ErrorCode.LOGGED_IN_ELSEWHERE, description = "다른 기기에서 로그인되어 세션이 종료된 경우")
  @ApiErrorResponse(value = ErrorCode.USER_NOT_FOUND, description = "사용자를 찾을 수 없는 경우")
  @ApiErrorResponse(value = ErrorCode.FORBIDDEN, description = "팀 계정이 아닌 경우")
  @ApiErrorResponse(value = ErrorCode.BOOTH_NOT_FOUND, description = "소속 프로젝트가 없거나 프로젝트에 배정된 부스가 없는 경우")
  @PostMapping
  ResponseEntity<SuccessResponse<QrTokenResponse>> issue(
      @Parameter(hidden = true) @AuthUser AuthPrincipal principal
  );
}
