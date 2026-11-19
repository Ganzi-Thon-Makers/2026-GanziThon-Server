package com.ganzithon.makers.platform.domain.project.infrastructure;

import com.ganzithon.makers.platform.domain.project.domain.Project;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  // 프로젝트명 부분 일치 검색 (%, _ 는 Spring Data 가 이스케이프)
  List<Project> findAllByTitleContainingIgnoreCase(String keyword);

  List<Project> findAllByTeamNumberIsNotNullOrderByTeamNumberAsc();

  boolean existsByTeamNumber(Integer teamNumber);

  boolean existsByTeamNumberAndIdNot(Integer teamNumber, Long id);

  // 프로젝트 삭제와 부스 배정처럼 직렬화가 필요한 작업에서 사용 (트랜잭션 종료까지 락 유지)
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Project p where p.id = :projectId")
  Optional<Project> findByIdForUpdate(@Param("projectId") Long projectId);
}
