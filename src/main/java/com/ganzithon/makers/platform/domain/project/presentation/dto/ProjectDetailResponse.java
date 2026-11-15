package com.ganzithon.makers.platform.domain.project.presentation.dto;

import com.ganzithon.makers.platform.domain.booth.domain.Booth;
import com.ganzithon.makers.platform.domain.booth.domain.Zone;
import com.ganzithon.makers.platform.domain.project.domain.Platform;
import com.ganzithon.makers.platform.domain.project.domain.Project;
import com.ganzithon.makers.platform.domain.user.domain.Part;
import java.util.List;
import java.util.Map;

public record ProjectDetailResponse(
    Long id,
    String title,
    String teamName,
    String summary,
    String description,
    String serviceUrl,
    String thumbnailUrl,
    String techStack,
    Platform platform,
    Zone zone,
    Integer boothNumber,
    Map<Part, List<String>> members
) {

  public static ProjectDetailResponse of(Project project, Booth booth, Map<Part, List<String>> members) {
    return new ProjectDetailResponse(
        project.getId(),
        project.getTitle(),
        project.getTeamName(),
        project.getSummary(),
        project.getDescription(),
        project.getServiceUrl(),
        project.getThumbnailUrl(),
        project.getTechStack(),
        project.getPlatform(),
        booth.getZone(),
        booth.getBoothNumber(),
        members
    );
  }
}
