package com.ganzithon.makers.platform.domain.stamp.presentation;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.common.security.AuthUser;
import com.ganzithon.makers.common.springdoc.ApiErrorResponse;
import com.ganzithon.makers.platform.domain.stamp.presentation.dto.StampBoardResponse;
import com.ganzithon.makers.platform.domain.stamp.presentation.dto.StampCollectRequest;
import com.ganzithon.makers.platform.domain.stamp.presentation.dto.StampCollectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Stamp", description = "부스 스탬프 / QR 토큰")
public interface StampApi {

  @Operation(
      summary = "스탬프 적립",
      description = "부스에 표시된 QR 토큰으로 스탬프를 적립합니다. 참가자만 적립할 수 있습니다. "
          + "이미 적립한 부스의 QR을 다시 찍으면 에러 대신 200과 함께 alreadyCollected: true, "
          + "boothId를 반환하므로 어느 부스인지 구분할 수 있습니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiErrorResponse(value = ErrorCode.UNAUTHORIZED, description = "인증되지 않은 경우")
  @ApiErrorResponse(value = ErrorCode.TOKEN_EXPIRED, description = "accessToken이 만료된 경우 (refresh로 재발급 필요)")
  @ApiErrorResponse(value = ErrorCode.LOGGED_IN_ELSEWHERE, description = "다른 기기에서 로그인되어 세션이 종료된 경우")
  @ApiErrorResponse(value = ErrorCode.INVALID_INPUT, description = "token이 비어있는 경우")
  @ApiErrorResponse(value = ErrorCode.STAMP_PARTICIPANT_ONLY, description = "참가자가 아닌 role로 적립한 경우")
  @ApiErrorResponse(value = ErrorCode.USER_NOT_FOUND, description = "사용자를 찾을 수 없는 경우")
  @ApiErrorResponse(value = ErrorCode.QR_TOKEN_NOT_FOUND, description = "유효하지 않은 QR 코드인 경우")
  @ApiErrorResponse(value = ErrorCode.QR_TOKEN_EXPIRED, description = "만료된 QR 코드인 경우")
  @ApiErrorResponse(value = ErrorCode.BOOTH_NOT_FOUND, description = "QR 토큰에 해당하는 부스가 없는 경우")
  @ApiErrorResponse(value = ErrorCode.STAMP_OWN_BOOTH, description = "본인 팀 부스의 스탬프를 적립한 경우")
  @ApiErrorResponse(value = ErrorCode.STAMP_COOLDOWN, description = "직전 적립 후 쿨다운(5분)이 지나지 않은 경우")
  @PostMapping
  ResponseEntity<SuccessResponse<StampCollectResponse>> collect(
      @Parameter(hidden = true) @AuthUser AuthPrincipal principal,
      @Valid @RequestBody StampCollectRequest requestBody
  );

  @Operation(
      summary = "내 스탬프판 조회",
      description = "존별 부스 목록과 각 부스의 적립 여부, 럭키드로우 진행 상황을 조회합니다. 참가자만 조회할 수 있습니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiErrorResponse(value = ErrorCode.UNAUTHORIZED, description = "인증되지 않은 경우")
  @ApiErrorResponse(value = ErrorCode.TOKEN_EXPIRED, description = "accessToken이 만료된 경우 (refresh로 재발급 필요)")
  @ApiErrorResponse(value = ErrorCode.LOGGED_IN_ELSEWHERE, description = "다른 기기에서 로그인되어 세션이 종료된 경우")
  @ApiErrorResponse(value = ErrorCode.STAMP_PARTICIPANT_ONLY, description = "참가자가 아닌 role로 조회한 경우")
  @ApiErrorResponse(value = ErrorCode.USER_NOT_FOUND, description = "사용자를 찾을 수 없는 경우")
  @GetMapping
  ResponseEntity<SuccessResponse<StampBoardResponse>> getBoard(
      @Parameter(hidden = true) @AuthUser AuthPrincipal principal
  );
}
