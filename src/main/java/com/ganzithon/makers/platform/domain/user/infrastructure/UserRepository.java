package com.ganzithon.makers.platform.domain.user.infrastructure;

import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.domain.User;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

  boolean existsByLoginId(String loginId);

  boolean existsByLoginIdAndIdNot(String loginId, Long id);

  // 여러 role 의 회원을 이름 부분 일치로 검색 (namePattern 이 null 이면 전체). 이스케이프 문자는 '!'
  @Query("""
      select u from User u
      where u.role in :roles
        and (:namePattern is null or u.name like :namePattern escape '!')
      order by u.name asc, u.id asc
      """)
  List<User> searchByRoles(
      @Param("roles") List<Role> roles,
      @Param("namePattern") String namePattern
  );

  // 조건이 null 이면 해당 조건은 적용하지 않음. namePattern 의 이스케이프 문자는 '!'
  @Query("""
      select u from User u
      where u.role = :role
        and (:namePattern is null or u.name like :namePattern escape '!')
        and (:luckyDrawEntered is null or u.luckyDrawEntered = :luckyDrawEntered)
        and (:completed is null or u.completed = :completed)
      order by u.name asc, u.id asc
      """)
  List<User> search(
      @Param("role") Role role,
      @Param("namePattern") String namePattern,
      @Param("luckyDrawEntered") Boolean luckyDrawEntered,
      @Param("completed") Boolean completed
  );
}
