package com.ganzithon.makers.backoffice.user.application;

import com.ganzithon.makers.backoffice.user.presentation.dto.UserListResponse;
import com.ganzithon.makers.platform.domain.user.application.UserService;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 참가자(PARTICIPANT) 목록 조회/검색 */
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
}
