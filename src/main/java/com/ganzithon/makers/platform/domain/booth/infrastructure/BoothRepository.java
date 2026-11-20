package com.ganzithon.makers.platform.domain.booth.infrastructure;

import com.ganzithon.makers.platform.domain.booth.domain.Booth;
import com.ganzithon.makers.platform.domain.booth.domain.Zone;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoothRepository extends JpaRepository<Booth, Long> {

  List<Booth> findAllByOrderByBoothNumberAsc();

  Optional<Booth> findByProjectId(Long projectId);

  List<Booth> findAllByOrderByZoneAscBoothNumberAsc();

  List<Booth> findAllByZoneOrderByBoothNumberAsc(Zone zone);

  boolean existsByZoneAndBoothNumberAndIdNot(Zone zone, Integer boothNumber, Long id);

  @Query("select b.projectId from Booth b where b.projectId is not null")
  List<Long> findAssignedProjectIds();

  // 프로젝트 삭제 시 부스는 유지하고 연결만 해제
  @Modifying
  @Query("update Booth b set b.projectId = null where b.projectId = :projectId")
  void clearProject(@Param("projectId") Long projectId);
}
