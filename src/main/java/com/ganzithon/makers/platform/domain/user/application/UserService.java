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
    //Team 배제
    private User findUserById(Long userId) {
        return userRepository.findByIdAndRoleNot(userId, Role.TEAM)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );
    }
}