package com.ganzithon.makers.platform.domain.project.presentation.dto;

import com.ganzithon.makers.platform.domain.booth.domain.Booth;
import com.ganzithon.makers.platform.domain.booth.domain.Zone;
import com.ganzithon.makers.platform.domain.project.domain.Platform;
import com.ganzithon.makers.platform.domain.project.domain.Project;
import com.ganzithon.makers.platform.domain.user.domain.Part;
import java.util.List;

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
    List<PartMembers> members
) {

  public record PartMembers(
      Part part,
      List<String> names
  ) {}

  public static ProjectDetailResponse of(Project project, Booth booth, List<PartMembers> members) {
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
