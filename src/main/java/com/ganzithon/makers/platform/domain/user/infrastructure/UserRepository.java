package com.ganzithon.makers.platform.domain.user.infrastructure;

import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  List<User> findAllByProjectId(Long projectId);

  List<User> findAllByProjectIdAndRole(Long projectId, Role role);

  // 프로젝트 팀원 선택용 후보 (참가자, 팀 계정). namePattern 이 null 이면 전체. 이스케이프 문자는 '!'
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

  Optional<User> findByLoginId(String loginId);

  // 프로젝트 삭제 시 소속 회원의 연결만 해제 (회원은 유지)
  @Modifying
  @Query("update User u set u.projectId = null where u.projectId = :projectId")
  void clearProject(@Param("projectId") Long projectId);

  Optional<User> findByIdAndRoleNot(Long id, Role role);

  List<User> findAllByTeamNumber(Integer teamNumber);
}
