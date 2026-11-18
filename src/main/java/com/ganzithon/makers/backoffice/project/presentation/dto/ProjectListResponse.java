package com.ganzithon.makers.backoffice.project.presentation.dto;

import com.ganzithon.makers.platform.domain.project.domain.Project;

public record ProjectListResponse(
    Long id,
    String title,
    String serviceUrl,
    long voteCount,
    String teamName,
    Integer teamNumber
) {

  public static ProjectListResponse of(Project project, long voteCount) {
    return new ProjectListResponse(
        project.getId(),
        project.getTitle(),
        project.getServiceUrl(),
        voteCount,
        project.getTeamName(),
        project.getTeamNumber()
    );
  }
}
