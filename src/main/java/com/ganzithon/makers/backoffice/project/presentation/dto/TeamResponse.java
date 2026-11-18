package com.ganzithon.makers.backoffice.project.presentation.dto;

import com.ganzithon.makers.platform.domain.project.domain.Project;

public record TeamResponse(
    Integer teamNumber,
    String teamName,
    Long projectId
) {

  public static TeamResponse of(Project project) {
    return new TeamResponse(project.getTeamNumber(), project.getTeamName(), project.getId());
  }
}
