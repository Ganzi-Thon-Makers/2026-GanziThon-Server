package com.ganzithon.makers.service.domain.user.infrastructure;

import com.ganzithon.makers.service.domain.user.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  List<User> findAllByProjectId(Long projectId);
}
