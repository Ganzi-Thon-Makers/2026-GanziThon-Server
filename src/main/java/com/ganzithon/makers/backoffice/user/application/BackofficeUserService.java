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
import com.ganzithon.makers.platform.domain.user.domain.Part;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.domain.User;
import com.ganzithon.makers.platform.domain.vote.application.VoteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 참가자 목록 조회/검색, 상세 조회, 수정, 삭제 / 계정(PARTICIPANT, TEAM, ADMIN) 등록 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BackofficeUserService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final AuthService authService;
  private final VoteService voteService;
  private final StampService stampService;

  // 등록 가능한 계정 (SUPER_ADMIN 제외). 조회·수정·삭제는 PARTICIPANT 만
  private static final List<Role> CREATABLE_ROLES = List.of(Role.PARTICIPANT, Role.TEAM, Role.ADMIN);

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
    Fields fields = resolveFields(validateCreatableRole(request.role()), request.name(), request.school(),
        request.phoneNumber(), request.teamNumber(), request.part());
    if (userService.existsByLoginId(request.loginId())) {
      throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
    }

    User user = User.createByAdmin(
        request.loginId(),
        passwordEncoder.encode(request.password()),
        fields.name(),
        fields.phoneNumber(),
        fields.school(),
        request.role(),
        fields.teamNumber(),
        fields.part()
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
    // role 은 변경 불가. 참가자 기준으로 필수 항목 검증
    Fields fields = resolveFields(user.getRole(), request.name(), request.school(),
        request.phoneNumber(), request.teamNumber(), request.part());
    if (userService.existsByLoginIdExcept(request.loginId(), userId)) {
      throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
    }

    user.update(
        request.loginId(),
        fields.name(),
        fields.phoneNumber(),
        fields.school(),
        fields.teamNumber(),
        fields.part()
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

  // 물리 삭제 (참가자만). 회원의 투표, 스탬프, 로그인 세션도 함께 삭제 (투표했던 프로젝트의 득표수가 줄어듦)
  // 프로젝트 팀원은 users.project_id 로만 연결되어 있어서 따로 처리할 것이 없음
  @Transactional
  public void delete(Long userId) {
    User user = getParticipant(userId);
    voteService.deleteAllByUserId(userId);
    stampService.deleteAllByUserId(userId);
    authService.logoutAll(userId);
    userService.delete(user);
  }

  // 조회·수정·삭제 대상은 참가자만. TEAM, ADMIN, SUPER_ADMIN 계정은 없는 회원과 같이 404
  private User getParticipant(Long userId) {
    User user = userService.getById(userId);
    if (user.getRole() != Role.PARTICIPANT) {
      throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }
    return user;
  }

  private Role validateCreatableRole(Role role) {
    if (!CREATABLE_ROLES.contains(role)) {
      throw new BusinessException(ErrorCode.INVALID_USER_ROLE);
    }
    return role;
  }

  // role 별 저장할 값. 필수 항목이 비어 있으면 400
  // - PARTICIPANT: 이름, 학교, 전화번호, 팀 번호, 파트 필수
  // - ADMIN: 이름, 학교, 전화번호 필수 / 파트 선택 / 팀 번호 무시
  // - TEAM: 팀 번호만 필수 / 이름은 "N팀", 학교·전화번호는 빈 값, 파트 없음
  private Fields resolveFields(Role role, String name, String school, String phoneNumber,
      Integer teamNumber, Part part) {
    return switch (role) {
      case PARTICIPANT -> {
        require(isNotBlank(name) && isNotBlank(school) && isNotBlank(phoneNumber)
            && teamNumber != null && part != null);
        yield new Fields(name, school, phoneNumber, teamNumber, part);
      }
      case ADMIN -> {
        require(isNotBlank(name) && isNotBlank(school) && isNotBlank(phoneNumber));
        yield new Fields(name, school, phoneNumber, null, part);
      }
      case TEAM -> {
        require(teamNumber != null);
        yield new Fields(teamNumber + "팀", "", "", teamNumber, null);
      }
      default -> throw new BusinessException(ErrorCode.INVALID_USER_ROLE);
    };
  }

  private static void require(boolean condition) {
    if (!condition) {
      throw new BusinessException(ErrorCode.MISSING_REQUIRED_USER_FIELD);
    }
  }

  private static boolean isNotBlank(String value) {
    return value != null && !value.isBlank();
  }

  private record Fields(String name, String school, String phoneNumber, Integer teamNumber, Part part) {
  }
}
