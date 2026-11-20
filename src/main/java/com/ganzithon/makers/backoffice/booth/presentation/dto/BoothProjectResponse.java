package com.ganzithon.makers.backoffice.booth.presentation.dto;

import com.ganzithon.makers.platform.domain.project.domain.Project;

// 부스에 배정된 프로젝트(팀) 요약
public record BoothProjectResponse(
    Long projectId,
    Integer teamNumber,
    String teamName,
    String title
) {

  public static BoothProjectResponse of(Project project) {
    return project == null ? null : new BoothProjectResponse(
        project.getId(),
        project.getTeamNumber(),
        project.getTeamName(),
        project.getTitle()
    );
  }
}
