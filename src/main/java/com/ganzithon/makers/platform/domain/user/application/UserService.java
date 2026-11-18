package com.ganzithon.makers.platform.domain.user.application;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.user.domain.User;
import com.ganzithon.makers.platform.domain.user.infrastructure.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;

  public User findById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }

  public List<User> findAllByProjectId(Long projectId) {
    return userRepository.findAllByProjectId(projectId);
  }

  public List<User> findAllByIds(List<Long> userIds) {
    return userRepository.findAllById(userIds);
  }

  @Transactional
  public void clearProject(Long projectId) {
    userRepository.clearProject(projectId);
  }
}
