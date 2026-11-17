package com.ganzithon.makers.platform.domain.user.application;

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

  public List<User> findAllByProjectId(Long projectId) {
    return userRepository.findAllByProjectId(projectId);
  }

  public List<User> findAllByIds(List<Long> userIds) {
    return userRepository.findAllById(userIds);
  }
}
