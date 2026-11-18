package com.ganzithon.makers.platform.domain.user.infrastructure;

import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.domain.User;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  List<User> findAllByProjectId(Long projectId);

  Optional<User> findByLoginId(String loginId);

  Optional<User> findByIdAndRoleNot(Long id, Role role);

  List<User> findAllByTeamNumber(Integer teamNumber);

  // 동일 회원의 스탬프 적립 등 직렬화가 필요한 작업에서 사용 (트랜잭션 종료까지 락 유지)
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from User u where u.id = :userId")
  Optional<User> findByIdForUpdate(@Param("userId") Long userId);
}
