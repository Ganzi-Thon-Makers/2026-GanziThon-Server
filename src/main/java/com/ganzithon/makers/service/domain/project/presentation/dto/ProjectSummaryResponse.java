package com.ganzithon.makers.service.domain.project.presentation.dto;

import com.ganzithon.makers.service.domain.project.domain.Platform;
import com.ganzithon.makers.service.domain.project.domain.Project;

public record ProjectSummaryResponse(
    Long id,
    String title,
    String teamName,
    String summary,
    String thumbnailUrl,
    Platform platform
) {

  public static ProjectSummaryResponse of(Project project) {
    return new ProjectSummaryResponse(
        project.getId(),
        project.getTitle(),
        project.getTeamName(),
        project.getSummary(),
        project.getThumbnailUrl(),
        project.getPlatform()
    );
  }
}
