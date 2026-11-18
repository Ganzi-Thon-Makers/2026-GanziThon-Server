package com.ganzithon.makers.platform.domain.project.presentation.dto;

import com.ganzithon.makers.platform.domain.project.domain.Platform;
import com.ganzithon.makers.platform.domain.project.domain.Project;

public record ProjectSummaryResponse(
    Long id,
    String title,
    String teamName,
    String summary,
    String thumbnailUrl,
    Platform platform,
    boolean isVoted
) {

  public static ProjectSummaryResponse of(Project project, boolean isVoted) {
    return new ProjectSummaryResponse(
        project.getId(),
        project.getTitle(),
        project.getTeamName(),
        project.getSummary(),
        project.getThumbnailUrl(),
        project.getPlatform(),
        isVoted
    );
  }
}
