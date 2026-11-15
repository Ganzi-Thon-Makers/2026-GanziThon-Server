package com.ganzithon.makers.backoffice.vote.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record VoteTimeUpdateRequest(

    @Schema(description = "투표 시작시간", example = "2026-11-21T14:00:00")
    @NotNull(message = "시작시간은 필수입니다.")
    LocalDateTime startTime,

    @Schema(description = "투표 마감시간 (시작시간보다 늦어야 함)", example = "2026-11-21T16:00:00")
    @NotNull(message = "마감시간은 필수입니다.")
    LocalDateTime endTime
) {
}
