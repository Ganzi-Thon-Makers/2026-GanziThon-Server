package com.ganzithon.makers.platform.domain.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.user.domain.Part;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.domain.User;
import com.ganzithon.makers.platform.domain.user.infrastructure.UserRepository;
import com.ganzithon.makers.platform.domain.user.presentation.dto.response.MyPageResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserServiceTest {

  private UserRepository userRepository;
  private UserService userService;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    userService = new UserService(userRepository);
  }

  private User user(Role role, Integer teamNumber) {
    return User.create("yhy6391", "encoded", "양화영", "01012345678", "메이커스대학교", role, teamNumber);
  }

  private User teamMember(String name, Part part, Integer teamNumber) {
    User user = User.create(name.toLowerCase(), "encoded", name, "01000000000", "메이커스대학교", Role.TEAM, teamNumber);
    user.assignToProject(1L, part);
    return user;
  }

  @Test
  @DisplayName("팀 소속 사용자는 본인 정보와 같은 팀번호의 팀원 목록을 받는다")
  void returnsMyPageWithTeamMembers() {
    User user = user(Role.PARTICIPANT, 3);
    when(userRepository.findByIdAndRoleNot(1L, Role.TEAM)).thenReturn(Optional.of(user));
    List<User> members = List.of(
        teamMember("김서연", Part.PLAN, 3),
        teamMember("박준혁", Part.SERVER, 3));
    when(userRepository.findAllByTeamNumber(3)).thenReturn(members);

    MyPageResponse response = userService.getMyPage(1L);

    assertThat(response.name()).isEqualTo("양화영");
    assertThat(response.loginId()).isEqualTo("yhy6391");
    assertThat(response.teamNumber()).isEqualTo(3);
    assertThat(response.teamMemberList()).hasSize(2);
    assertThat(response.teamMemberList().get(0).name()).isEqualTo("김서연");
    assertThat(response.teamMemberList().get(0).part()).isEqualTo(Part.PLAN);
  }

  @Test
  @DisplayName("팀번호가 없는 사용자는 팀원 조회 없이 빈 목록을 받는다")
  void noTeamNumberReturnsEmptyMembers() {
    User user = user(Role.PARTICIPANT, null);
    when(userRepository.findByIdAndRoleNot(1L, Role.TEAM)).thenReturn(Optional.of(user));

    MyPageResponse response = userService.getMyPage(1L);

    assertThat(response.teamNumber()).isNull();
    assertThat(response.teamMemberList()).isEmpty();
    verify(userRepository, never()).findAllByTeamNumber(anyInt());
  }

  @Test
  @DisplayName("같은 팀번호의 팀원만 포함된다")
  void onlySameTeamNumberMembers() {
    User user = user(Role.PARTICIPANT, 5);
    when(userRepository.findByIdAndRoleNot(1L, Role.TEAM)).thenReturn(Optional.of(user));
    when(userRepository.findAllByTeamNumber(5))
        .thenReturn(List.of(teamMember("정다은", Part.WEB, 5)));

    MyPageResponse response = userService.getMyPage(1L);

    assertThat(response.teamMemberList()).hasSize(1);
    verify(userRepository).findAllByTeamNumber(5);
  }

  @Test
  @DisplayName("TEAM 계정이면 USER_NOT_FOUND")
  void teamRoleThrowsUserNotFound() {
    when(userRepository.findByIdAndRoleNot(1L, Role.TEAM)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getMyPage(1L))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
  }

  @Test
  @DisplayName("존재하지 않는 사용자면 USER_NOT_FOUND")
  void unknownUserThrowsUserNotFound() {
    when(userRepository.findByIdAndRoleNot(99L, Role.TEAM)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getMyPage(99L))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
  }
}
