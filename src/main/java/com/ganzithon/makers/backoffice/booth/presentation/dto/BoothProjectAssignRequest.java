package com.ganzithon.makers.backoffice.booth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record BoothProjectAssignRequest(
    @Schema(description = "배정할 프로젝트 id", example = "1")
    @NotNull
    Long projectId
) {
}
