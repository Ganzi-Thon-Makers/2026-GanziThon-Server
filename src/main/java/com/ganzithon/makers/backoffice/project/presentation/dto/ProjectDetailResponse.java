package com.ganzithon.makers.backoffice.project.presentation.dto;

import com.ganzithon.makers.platform.domain.project.domain.Platform;
import com.ganzithon.makers.platform.domain.project.domain.Project;
import com.ganzithon.makers.platform.domain.user.domain.Part;
import com.ganzithon.makers.platform.domain.user.domain.User;
import java.util.List;

public record ProjectDetailResponse(
    Long id,
    String title,
    String thumbnailUrl,
    String serviceUrl,
    long voteCount,
    String teamName,
    List<Member> members,
    String techStack,
    String summary,
    String description,
    Platform platform
) {

  // userId 는 수정 화면에서 기존 팀원을 그대로 다시 보낼 때 사용
  public record Member(
      Long userId,
      Part part,
      String name
  ) {

    public static Member of(User user) {
      return new Member(user.getId(), user.getPart(), user.getName());
    }
  }

  public static ProjectDetailResponse of(Project project, long voteCount, List<Member> members) {
    return new ProjectDetailResponse(
        project.getId(),
        project.getTitle(),
        project.getThumbnailUrl(),
        project.getServiceUrl(),
        voteCount,
        project.getTeamName(),
        members,
        project.getTechStack(),
        project.getSummary(),
        project.getDescription(),
        project.getPlatform()
    );
  }
}
