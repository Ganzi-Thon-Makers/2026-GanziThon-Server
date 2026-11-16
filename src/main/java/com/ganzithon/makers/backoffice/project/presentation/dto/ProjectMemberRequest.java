package com.ganzithon.makers.backoffice.project.presentation.dto;

import com.ganzithon.makers.platform.domain.user.domain.Part;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

// 프로젝트 등록/수정 시 팀원 한 명
public record ProjectMemberRequest(
    @Schema(example = "1")
    @NotNull
    Long userId,

    @Schema(example = "PLAN")
    @NotNull
    Part part
) {}
