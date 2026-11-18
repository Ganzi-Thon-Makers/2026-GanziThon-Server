package com.ganzithon.makers.backoffice.user.presentation.dto;

import com.ganzithon.makers.platform.domain.user.domain.Part;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.domain.User;

public record AccountListResponse(
    Long id,
    String loginId,
    Role role,
    String name,
    String school,
    String phoneNumber,
    Integer teamNumber,
    Part part
) {

  public static AccountListResponse of(User user) {
    return new AccountListResponse(
        user.getId(),
        user.getLoginId(),
        user.getRole(),
        user.getName(),
        user.getSchool(),
        user.getPhoneNumber(),
        user.getTeamNumber(),
        user.getPart()
    );
  }
}
