package com.ganzithon.makers.platform.domain.timetable.presentation;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.common.springdoc.ApiErrorResponse;
import com.ganzithon.makers.platform.domain.timetable.presentation.dto.response.TimetableAllResponse;
import com.ganzithon.makers.platform.domain.timetable.presentation.dto.response.TimetableDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Timetable", description = "타임테이블 조회 API")
public interface TimetableApi {

    @Operation(
            summary = "타임테이블 전체 조회",
            description = "모든 일정을 시작 시간 오름차순으로 조회합니다. 시간은 HH:mm 형식으로 반환됩니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiErrorResponse(value = ErrorCode.UNAUTHORIZED, description = "인증되지 않은 경우")
    @ApiErrorResponse(value = ErrorCode.TOKEN_EXPIRED, description = "accessToken이 만료된 경우 (refresh로 재발급 필요)")
    @ApiErrorResponse(value = ErrorCode.LOGGED_IN_ELSEWHERE, description = "다른 기기에서 로그인되어 세션이 종료된 경우")
    @GetMapping
    ResponseEntity<SuccessResponse<TimetableAllResponse>> getAllTimeTables();

    @Operation(
            summary = "타임테이블 상세 조회",
            description = "일정 ID로 상세 정보(제목, 설명, 시간)를 조회합니다. 시간은 HH:mm 형식으로 반환됩니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiErrorResponse(value = ErrorCode.UNAUTHORIZED, description = "인증되지 않은 경우")
    @ApiErrorResponse(value = ErrorCode.TOKEN_EXPIRED, description = "accessToken이 만료된 경우 (refresh로 재발급 필요)")
    @ApiErrorResponse(value = ErrorCode.LOGGED_IN_ELSEWHERE, description = "다른 기기에서 로그인되어 세션이 종료된 경우")
    @ApiErrorResponse(value = ErrorCode.TIMETABLE_NOT_FOUND, description = "해당 ID의 일정이 없는 경우")
    @GetMapping("/{timetableId}")
    ResponseEntity<SuccessResponse<TimetableDetailResponse>> getTimeTable(
            @Parameter(description = "조회할 일정 ID", example = "1") @PathVariable Long timetableId);
}
