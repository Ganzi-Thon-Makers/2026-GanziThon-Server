package com.ganzithon.makers.backoffice.booth.presentation.dto;

import com.ganzithon.makers.platform.domain.booth.domain.Booth;
import com.ganzithon.makers.platform.domain.booth.domain.Zone;
import com.ganzithon.makers.platform.domain.project.domain.Project;

// project: 배정된 프로젝트, 비어 있으면 null
public record BoothAssignResponse(
    Long boothId,
    Zone zone,
    String roomNumber,
    Integer boothNumber,
    BoothProjectResponse project
) {

  public static BoothAssignResponse of(Booth booth, Project project) {
    return new BoothAssignResponse(
        booth.getId(),
        booth.getZone(),
        booth.getRoomNumber(),
        booth.getBoothNumber(),
        BoothProjectResponse.of(project)
    );
  }
}
