package com.ganzithon.makers.platform.domain.project.presentation;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.common.security.AuthUser;
import com.ganzithon.makers.common.springdoc.ApiErrorResponse;
import com.ganzithon.makers.platform.domain.booth.domain.Zone;
import com.ganzithon.makers.platform.domain.project.presentation.dto.ProjectDetailResponse;
import com.ganzithon.makers.platform.domain.project.presentation.dto.ProjectSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Project", description = "프로젝트 API")
public interface ProjectApi {

  @Operation(
      summary = "프로젝트 목록 조회",
      description = "존별로 그룹핑된 프로젝트 목록을 조회합니다. 각 프로젝트에는 로그인한 사용자의 투표 여부(isVoted)가 포함됩니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiErrorResponse(value = ErrorCode.UNAUTHORIZED, description = "인증되지 않은 경우")
  @ApiErrorResponse(value = ErrorCode.TOKEN_EXPIRED, description = "accessToken이 만료된 경우 (refresh로 재발급 필요)")
  @ApiErrorResponse(value = ErrorCode.LOGGED_IN_ELSEWHERE, description = "다른 기기에서 로그인되어 세션이 종료된 경우")
  @GetMapping
  ResponseEntity<SuccessResponse<Map<Zone, List<ProjectSummaryResponse>>>> getProjects(
      @Parameter(hidden = true) @AuthUser AuthPrincipal principal);

  @Operation(
      summary = "내 프로젝트 조회",
      description = "로그인한 사용자가 소속된 프로젝트의 상세 정보를 조회합니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiErrorResponse(value = ErrorCode.UNAUTHORIZED, description = "인증되지 않은 경우")
  @ApiErrorResponse(value = ErrorCode.TOKEN_EXPIRED, description = "accessToken이 만료된 경우 (refresh로 재발급 필요)")
  @ApiErrorResponse(value = ErrorCode.LOGGED_IN_ELSEWHERE, description = "다른 기기에서 로그인되어 세션이 종료된 경우")
  @ApiErrorResponse(value = ErrorCode.USER_NOT_FOUND, description = "사용자를 찾을 수 없는 경우")
  @ApiErrorResponse(value = ErrorCode.PROJECT_NOT_FOUND, description = "소속된 프로젝트가 없는 경우")
  @ApiErrorResponse(value = ErrorCode.BOOTH_NOT_FOUND, description = "프로젝트에 배정된 부스가 없는 경우")
  @GetMapping("/me")
  ResponseEntity<SuccessResponse<ProjectDetailResponse>> getMyProject(
      @Parameter(hidden = true) @AuthUser AuthPrincipal principal);

  @Operation(
      summary = "프로젝트 상세 조회",
      description = "프로젝트 ID로 상세 정보를 조회합니다. 부스 정보와 파트별 팀원 목록이 포함됩니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiErrorResponse(value = ErrorCode.UNAUTHORIZED, description = "인증되지 않은 경우")
  @ApiErrorResponse(value = ErrorCode.TOKEN_EXPIRED, description = "accessToken이 만료된 경우 (refresh로 재발급 필요)")
  @ApiErrorResponse(value = ErrorCode.LOGGED_IN_ELSEWHERE, description = "다른 기기에서 로그인되어 세션이 종료된 경우")
  @ApiErrorResponse(value = ErrorCode.PROJECT_NOT_FOUND, description = "프로젝트를 찾을 수 없는 경우")
  @ApiErrorResponse(value = ErrorCode.BOOTH_NOT_FOUND, description = "프로젝트에 배정된 부스가 없는 경우")
  @GetMapping("/{projectId}")
  ResponseEntity<SuccessResponse<ProjectDetailResponse>> getProjectDetail(@PathVariable Long projectId);
}
