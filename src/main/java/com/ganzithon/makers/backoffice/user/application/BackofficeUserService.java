package com.ganzithon.makers.backoffice.user.application;

import com.ganzithon.makers.backoffice.user.presentation.dto.UserCreateRequest;
import com.ganzithon.makers.backoffice.user.presentation.dto.UserDetailResponse;
import com.ganzithon.makers.backoffice.user.presentation.dto.UserListResponse;
import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.user.application.UserService;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 참가자(PARTICIPANT) 목록 조회/검색, 상세 조회, 등록, 수정, 삭제 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BackofficeUserService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;

  // 관리 대상은 참가자만. 조건이 null 이면 해당 조건은 적용하지 않음
  public List<UserListResponse> getUsers(String keyword, Boolean luckyDrawEntered, Boolean completed) {
    return userService.search(Role.PARTICIPANT, keyword, luckyDrawEntered, completed).stream()
        .map(UserListResponse::of)
        .toList();
  }

  public UserDetailResponse getUser(Long userId) {
    return UserDetailResponse.of(getParticipant(userId));
  }

  @Transactional
  public UserDetailResponse create(UserCreateRequest request) {
    if (userService.existsByLoginId(request.loginId())) {
      throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
    }

    User user = User.createParticipant(
        request.loginId(),
        passwordEncoder.encode(request.password()),
        request.name(),
        request.phoneNumber(),
        request.school(),
        request.teamNumber(),
        request.part()
    );
    try {
      return UserDetailResponse.of(userService.save(user));
    } catch (DataIntegrityViolationException e) {
      // 동시 요청으로 위 중복 확인을 함께 통과한 경우, DB UNIQUE 제약(login_id)이 막는다
      throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
    }
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
