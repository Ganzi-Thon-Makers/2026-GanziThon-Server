package com.ganzithon.makers.backoffice.timetable.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record TimetableUpdateRequest(

    @Schema(description = "제목", example = "오프닝 및 오리엔테이션")
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    String title,

    @Schema(description = "상세 내용 (null이면 비워짐)", example = "행사 진행 방식과 전체 일정을 안내합니다.", nullable = true)
    String description,

    @Schema(description = "시작시간", example = "2026-11-21T10:30:00")
    @NotNull(message = "시작시간은 필수입니다.")
    LocalDateTime startTime,

    @Schema(description = "종료시간 (시작시간보다 늦어야 함)", example = "2026-11-21T11:00:00")
    @NotNull(message = "종료시간은 필수입니다.")
    LocalDateTime endTime
) {
}
