package com.ganzithon.makers.backoffice.project.presentation.dto;

import com.ganzithon.makers.platform.domain.user.domain.Part;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.domain.User;

// projectId: 현재 소속 프로젝트 (null 이면 미소속)
public record MemberCandidateResponse(
    Long userId,
    String name,
    String loginId,
    Role role,
    Part part,
    Integer teamNumber,
    Long projectId
) {

  public static MemberCandidateResponse of(User user) {
    return new MemberCandidateResponse(
        user.getId(),
        user.getName(),
        user.getLoginId(),
        user.getRole(),
        user.getPart(),
        user.getTeamNumber(),
        user.getProjectId()
    );
  }
}
