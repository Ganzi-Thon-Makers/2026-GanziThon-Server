package com.ganzithon.makers.backoffice.booth.presentation.dto;

import com.ganzithon.makers.platform.domain.booth.domain.Booth;
import com.ganzithon.makers.platform.domain.booth.domain.Zone;
import com.ganzithon.makers.platform.domain.project.domain.Project;

// 부스 배정·수정 결과. project: 배정된 프로젝트, 비어 있으면 null
public record BoothResponse(
    Long boothId,
    Zone zone,
    String roomNumber,
    Integer boothNumber,
    String location,
    BoothProjectResponse project
) {
  public static BoothResponse of(Booth booth, Project project) {
    return new BoothResponse(
        booth.getId(),
        booth.getZone(),
        booth.getRoomNumber(),
        booth.getBoothNumber(),
        booth.getLocation(),
        BoothProjectResponse.of(project)
    );
  }
}
