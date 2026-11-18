package com.ganzithon.makers.platform.domain.user.application;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.user.domain.Role;
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

  public User getById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }

  public List<User> findAllByIds(List<Long> userIds) {
      return userRepository.findAllById(userIds);
  }

  public boolean existsByLoginId(String loginId) {
    return userRepository.existsByLoginId(loginId);
  }

  // 수정 시 자기 자신은 제외하고 아이디 중복 확인
  public boolean existsByLoginIdExcept(String loginId, Long userId) {
    return userRepository.existsByLoginIdAndIdNot(loginId, userId);
  }

  // login_id UNIQUE 위반을 호출한 쪽에서 바로 잡을 수 있도록 즉시 flush (수정된 엔티티도 이걸로 반영)
  @Transactional
  public User save(User user) {
    return userRepository.saveAndFlush(user);
  }

  @Transactional
  public void delete(User user) {
    userRepository.delete(user);
  }

  // 이름 부분 일치 + 럭키드로우 응모/수료 여부 필터 (null 인 조건은 적용 안 함). 이름 가나다순
  public List<User> search(Role role, String nameKeyword, Boolean luckyDrawEntered, Boolean completed) {
    return userRepository.search(role, toContainsPattern(nameKeyword), luckyDrawEntered, completed);
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

  // 동시 적립 요청을 직렬화해야 할 때 사용. 트랜잭션 커밋까지 행 락을 유지한다
  public User getByIdForUpdate(Long userId) {
    return userRepository.findByIdForUpdate(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }
}
