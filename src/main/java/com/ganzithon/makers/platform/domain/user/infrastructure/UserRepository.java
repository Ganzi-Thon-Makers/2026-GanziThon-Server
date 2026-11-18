package com.ganzithon.makers.platform.domain.user.infrastructure;

import com.ganzithon.makers.platform.domain.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  List<User> findAllByProjectId(Long projectId);

  Optional<User> findByLoginId(String loginId);

  // 프로젝트 삭제 시 소속 회원의 연결만 해제 (회원은 유지)
  @Modifying
  @Query("update User u set u.projectId = null where u.projectId = :projectId")
  void clearProject(@Param("projectId") Long projectId);
}
