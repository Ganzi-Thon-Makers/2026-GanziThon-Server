package com.ganzithon.makers.platform.domain.vote.presentation;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.common.security.AuthUser;
import com.ganzithon.makers.common.springdoc.ApiErrorResponse;
import com.ganzithon.makers.platform.domain.vote.presentation.dto.VotePeriodResponse;
import com.ganzithon.makers.platform.domain.vote.presentation.dto.VoteSubmitRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Vote", description = "프로젝트 인기투표")
public interface VoteApi {

  @Operation(
      summary = "프로젝트 투표",
      description = "참가자만 투표할 수 있으며, 사용자당 1표만 가능합니다. "
          + "본인 팀 프로젝트에는 투표할 수 없고, 어드민이 설정한 투표 시간에만 가능합니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiErrorResponse(value = ErrorCode.UNAUTHORIZED, description = "인증되지 않은 경우")
  @ApiErrorResponse(value = ErrorCode.TOKEN_EXPIRED, description = "accessToken이 만료된 경우 (refresh로 재발급 필요)")
  @ApiErrorResponse(value = ErrorCode.LOGGED_IN_ELSEWHERE, description = "다른 기기에서 로그인되어 세션이 종료된 경우")
  @ApiErrorResponse(value = ErrorCode.INVALID_INPUT, description = "projectId가 비어있는 경우")
  @ApiErrorResponse(value = ErrorCode.VOTE_PARTICIPANT_ONLY, description = "참가자가 아닌 role로 투표한 경우")
  @ApiErrorResponse(value = ErrorCode.USER_NOT_FOUND, description = "사용자를 찾을 수 없는 경우")
  @ApiErrorResponse(value = ErrorCode.PROJECT_NOT_FOUND, description = "프로젝트를 찾을 수 없는 경우")
  @ApiErrorResponse(value = ErrorCode.VOTE_OWN_PROJECT, description = "본인 팀 프로젝트에 투표한 경우")
  @ApiErrorResponse(value = ErrorCode.VOTE_NOT_IN_PROGRESS, description = "투표 시간이 아닌 경우")
  @ApiErrorResponse(value = ErrorCode.VOTE_ALREADY_CAST, description = "이미 투표를 완료한 경우")
  @PostMapping
  ResponseEntity<SuccessResponse<Void>> submit(
      @Parameter(hidden = true) @AuthUser AuthPrincipal principal,
      @Valid @RequestBody VoteSubmitRequest request
  );

  @Operation(
      summary = "투표 기간 조회",
      description = "어드민이 등록한 투표 시작, 종료 시각과 현재 상태(NOT_STARTED/IN_PROGRESS/PAUSED/ENDED)를 조회합니다. "
          + "PAUSED 상태에서는 투표가 불가능합니다. "
          + "일정이 아직 등록되지 않은 경우에도 status=NOT_STARTED와 null 시간으로 응답하며, "
          + "serverTime은 카운트다운 시 클라이언트 시계 오차 방지용입니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiErrorResponse(value = ErrorCode.UNAUTHORIZED, description = "인증되지 않은 경우")
  @ApiErrorResponse(value = ErrorCode.TOKEN_EXPIRED, description = "accessToken이 만료된 경우 (refresh로 재발급 필요)")
  @ApiErrorResponse(value = ErrorCode.LOGGED_IN_ELSEWHERE, description = "다른 기기에서 로그인되어 세션이 종료된 경우")
  @GetMapping("/period")
  ResponseEntity<SuccessResponse<VotePeriodResponse>> getPeriod();
}
