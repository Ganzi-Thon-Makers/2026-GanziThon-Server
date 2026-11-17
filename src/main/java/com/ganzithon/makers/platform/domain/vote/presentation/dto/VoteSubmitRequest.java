package com.ganzithon.makers.platform.domain.vote.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record VoteSubmitRequest(

    @Schema(description = "투표할 프로젝트 ID", example = "1")
    @NotNull(message = "프로젝트 ID는 필수입니다.")
    Long projectId
) {
}
