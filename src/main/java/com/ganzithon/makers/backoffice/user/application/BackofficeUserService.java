package com.ganzithon.makers.backoffice.user.application;

import com.ganzithon.makers.backoffice.user.presentation.dto.UserDetailResponse;
import com.ganzithon.makers.backoffice.user.presentation.dto.UserListResponse;
import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.user.application.UserService;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 참가자(PARTICIPANT) 목록 조회/검색, 상세 조회, 수정, 삭제 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BackofficeUserService {

  private final UserService userService;

  // 관리 대상은 참가자만. 조건이 null 이면 해당 조건은 적용하지 않음
  public List<UserListResponse> getUsers(String keyword, Boolean luckyDrawEntered, Boolean completed) {
    return userService.search(Role.PARTICIPANT, keyword, luckyDrawEntered, completed).stream()
        .map(UserListResponse::of)
        .toList();
  }

  public UserDetailResponse getUser(Long userId) {
    return UserDetailResponse.of(getParticipant(userId));
  }

  // 참가자가 아닌 계정(TEAM, ADMIN 등)은 관리 대상이 아니므로 없는 회원과 같이 404
  private User getParticipant(Long userId) {
    User user = userService.getById(userId);
    if (user.getRole() != Role.PARTICIPANT) {
      throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }
    return user;
  }
}
