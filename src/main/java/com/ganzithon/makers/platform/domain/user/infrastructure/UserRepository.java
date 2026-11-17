package com.ganzithon.makers.platform.domain.user.infrastructure;

import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  List<User> findAllByProjectId(Long projectId);

  Optional<User> findByLoginId(String loginId);

  boolean existsByLoginId(String loginId);

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
