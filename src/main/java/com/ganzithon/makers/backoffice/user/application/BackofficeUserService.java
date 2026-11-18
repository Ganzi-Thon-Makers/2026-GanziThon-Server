package com.ganzithon.makers.backoffice.user.application;

import com.ganzithon.makers.backoffice.user.presentation.dto.UserCreateRequest;
import com.ganzithon.makers.backoffice.user.presentation.dto.UserDetailResponse;
import com.ganzithon.makers.backoffice.user.presentation.dto.UserListResponse;
import com.ganzithon.makers.backoffice.user.presentation.dto.UserUpdateRequest;
import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.auth.application.AuthService;
import com.ganzithon.makers.platform.domain.stamp.application.StampService;
import com.ganzithon.makers.platform.domain.user.application.UserService;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.domain.User;
import com.ganzithon.makers.platform.domain.vote.application.VoteService;
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
  private final AuthService authService;
  private final VoteService voteService;
  private final StampService stampService;

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

  // 전체 교체. 비밀번호는 보낸 경우에만 바꾸고, 바꾸면 해당 회원의 로그인 세션을 모두 지움 (강제 로그아웃)
  @Transactional
  public UserDetailResponse update(Long userId, UserUpdateRequest request) {
    User user = getParticipant(userId);
    if (userService.existsByLoginIdExcept(request.loginId(), userId)) {
      throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
    }

    user.update(
        request.loginId(),
        request.name(),
        request.phoneNumber(),
        request.school(),
        request.teamNumber(),
        request.part()
    );
    boolean passwordChanged = request.password() != null;
    if (passwordChanged) {
      user.changePassword(passwordEncoder.encode(request.password()));
    }

    try {
      userService.save(user);
    } catch (DataIntegrityViolationException e) {
      // 동시 요청으로 위 중복 확인을 함께 통과한 경우, DB UNIQUE 제약(login_id)이 막는다
      throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
    }

    if (passwordChanged) {
      authService.logoutAll(userId);
    }
    return UserDetailResponse.of(user);
  }

  // 물리 삭제. 회원의 투표, 스탬프, 로그인 세션도 함께 삭제 (투표했던 프로젝트의 득표수가 줄어듦)
  // 프로젝트 팀원은 users.project_id 로만 연결되어 있어서 따로 처리할 것이 없음
  @Transactional
  public void delete(Long userId) {
    User user = getParticipant(userId);
    voteService.deleteAllByUserId(userId);
    stampService.deleteAllByUserId(userId);
    authService.logoutAll(userId);
    userService.delete(user);
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
