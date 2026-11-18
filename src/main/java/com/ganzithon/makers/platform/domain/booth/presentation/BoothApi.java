package com.ganzithon.makers.platform.domain.booth.presentation;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.common.springdoc.ApiErrorResponse;
import com.ganzithon.makers.platform.domain.booth.presentation.response.BoothDetailResponse;
import com.ganzithon.makers.platform.domain.booth.presentation.response.BoothListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Booth", description = "부스 API")
public interface BoothApi {

  @Operation(
      summary = "부스 전체 조회",
      description = "전체 부스 목록 Zone별로 그룹핑하여 조회합니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiErrorResponse(value = ErrorCode.UNAUTHORIZED, description = "인증되지 않은 경우")
  @ApiErrorResponse(value = ErrorCode.TOKEN_EXPIRED, description = "accessToken이 만료된 경우 (refresh로 재발급 필요)")
  @ApiErrorResponse(value = ErrorCode.LOGGED_IN_ELSEWHERE, description = "다른 기기에서 로그인되어 세션이 종료된 경우")
  @GetMapping
  ResponseEntity<SuccessResponse<BoothListResponse>> getBoothsAll();

  @Operation(
      summary = "부스 상세 조회",
      description = "부스 ID로 부스에 배정된 프로젝트의 상세 정보를 조회합니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiErrorResponse(value = ErrorCode.UNAUTHORIZED, description = "인증되지 않은 경우")
  @ApiErrorResponse(value = ErrorCode.TOKEN_EXPIRED, description = "accessToken이 만료된 경우 (refresh로 재발급 필요)")
  @ApiErrorResponse(value = ErrorCode.LOGGED_IN_ELSEWHERE, description = "다른 기기에서 로그인되어 세션이 종료된 경우")
  @ApiErrorResponse(value = ErrorCode.BOOTH_NOT_FOUND, description = "부스를 찾을 수 없는 경우")
  @ApiErrorResponse(value = ErrorCode.PROJECT_NOT_FOUND, description = "부스에 배정된 프로젝트가 없는 경우")
  @GetMapping("/{boothId}")
  ResponseEntity<SuccessResponse<BoothDetailResponse>> getBoothDetail(@PathVariable Long boothId);
}
