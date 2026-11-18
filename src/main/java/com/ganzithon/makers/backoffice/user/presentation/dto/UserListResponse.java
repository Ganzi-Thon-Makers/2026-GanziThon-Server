package com.ganzithon.makers.backoffice.user.presentation.dto;

import com.ganzithon.makers.platform.domain.user.domain.User;

public record UserListResponse(
    Long id,
    String name,
    String school,
    String phoneNumber,
    boolean luckyDrawEntered,
    boolean completed,
    Integer teamNumber
) {

  public static UserListResponse of(User user) {
    return new UserListResponse(
        user.getId(),
        user.getName(),
        user.getSchool(),
        user.getPhoneNumber(),
        user.isLuckyDrawEntered(),
        user.isCompleted(),
        user.getTeamNumber()
    );
  }
}
