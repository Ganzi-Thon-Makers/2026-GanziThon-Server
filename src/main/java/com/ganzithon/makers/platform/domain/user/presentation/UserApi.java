package com.ganzithon.makers.platform.domain.user.presentation;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.common.security.AuthUser;
import com.ganzithon.makers.common.springdoc.ApiErrorResponse;
import com.ganzithon.makers.platform.domain.user.presentation.dto.response.MyPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "User", description = "사용자 API")
public interface UserApi {

    @Operation(
            summary = "마이페이지 조회",
            description = "로그인한 사용자의 정보와 소속 팀의 팀원 목록을 조회합니다. "
                    + "팀에 소속되지 않은 사용자는 teamNumber와 teamMemberList가 비어 있습니다. "
                    + "TEAM 계정은 조회할 수 없습니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiErrorResponse(value = ErrorCode.UNAUTHORIZED, description = "인증되지 않은 경우")
    @ApiErrorResponse(value = ErrorCode.TOKEN_EXPIRED, description = "accessToken이 만료된 경우 (refresh로 재발급 필요)")
    @ApiErrorResponse(value = ErrorCode.LOGGED_IN_ELSEWHERE, description = "다른 기기에서 로그인되어 세션이 종료된 경우")
    @ApiErrorResponse(value = ErrorCode.USER_NOT_FOUND, description = "회원을 찾을 수 없거나 TEAM 계정인 경우")
    @GetMapping("/mypage")
    ResponseEntity<SuccessResponse<MyPageResponse>> getMyPage(
            @Parameter(hidden = true) @AuthUser AuthPrincipal principal);
}
