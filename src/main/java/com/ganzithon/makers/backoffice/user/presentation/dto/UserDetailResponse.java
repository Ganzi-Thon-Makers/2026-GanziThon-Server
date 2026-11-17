package com.ganzithon.makers.backoffice.user.presentation.dto;

import com.ganzithon.makers.platform.domain.user.domain.Part;
import com.ganzithon.makers.platform.domain.user.domain.User;

public record UserDetailResponse(
    Long id,
    String loginId,
    String name,
    String school,
    String phoneNumber,
    Integer teamNumber,
    Part part,
    boolean luckyDrawEntered,
    boolean completed
) {

  public static UserDetailResponse of(User user) {
    return new UserDetailResponse(
        user.getId(),
        user.getLoginId(),
        user.getName(),
        user.getSchool(),
        user.getPhoneNumber(),
        user.getTeamNumber(),
        user.getPart(),
        user.isLuckyDrawEntered(),
        user.isCompleted()
    );
  }
}
