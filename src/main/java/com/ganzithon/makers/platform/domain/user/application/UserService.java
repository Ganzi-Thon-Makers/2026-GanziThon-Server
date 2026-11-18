package com.ganzithon.makers.platform.domain.user.application;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.user.application.dto.MyPageInfo;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.domain.User;
import com.ganzithon.makers.platform.domain.user.infrastructure.UserRepository;
import com.ganzithon.makers.platform.domain.user.presentation.dto.response.MyPageResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

  public MyPageResponse getMyPage(Long userId) {
        User user = findUserById(userId);
        Integer teamNumber = user.getTeamNumber();

        List<MyPageInfo.TeamMember> teamMembers =
                teamNumber == null
                        ? List.of()
                        : userRepository.findAllByTeamNumber(teamNumber)
                                .stream()
                                .map(MyPageInfo.TeamMember::from)
                                .toList();

        MyPageInfo myPageInfo = MyPageInfo.from(user, teamMembers);

        return MyPageResponse.from(myPageInfo);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );
    }

    public List<User> findAllByProjectId(Long projectId) {
        return userRepository.findAllByProjectId(projectId);
    }

    public List<User> findAllByIds(List<Long> userIds) {
        return userRepository.findAllById(userIds);
    }

  public List<User> findAllByProjectIdAndRole(Long projectId, Role role) {
    return userRepository.findAllByProjectIdAndRole(projectId, role);
  }

  // 이름 부분 일치(비어 있으면 전체), 이름 가나다순
  public List<User> searchByRoles(List<Role> roles, String nameKeyword) {
    return userRepository.searchByRoles(roles, toContainsPattern(nameKeyword));
  }

  // 검색어의 %, _ 를 글자 그대로 찾도록 이스케이프 ('!' 사용). 비어 있으면 null
  private static String toContainsPattern(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return null;
    }
    String escaped = keyword.strip()
        .replace("!", "!!")
        .replace("%", "!%")
        .replace("_", "!_");
    return "%" + escaped + "%";
  }
    //Team 배제
    private User findUserById(Long userId) {
        return userRepository.findByIdAndRoleNot(userId, Role.TEAM)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );
    }

  @Transactional
  public void clearProject(Long projectId) {
    userRepository.clearProject(projectId);
  }
}