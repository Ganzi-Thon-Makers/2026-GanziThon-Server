package com.ganzithon.makers.platform.domain.user.infrastructure;

import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  List<User> findAllByProjectId(Long projectId);

  Optional<User> findByLoginId(String loginId);

  Optional<User> findByIdAndRoleNot(Long id, Role role);

  List<User> findAllByTeamNumber(Integer teamNumber);
}
