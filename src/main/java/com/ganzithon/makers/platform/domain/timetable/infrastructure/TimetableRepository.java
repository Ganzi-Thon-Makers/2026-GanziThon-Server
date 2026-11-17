package com.ganzithon.makers.platform.domain.timetable.infrastructure;

import com.ganzithon.makers.platform.domain.timetable.domain.Timetable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {

    // 순서가 항상 같도록 시작 → 종료 → id 순으로 정렬
    List<Timetable> findAllByOrderByStartTimeAscEndTimeAscIdAsc();

    Optional<Timetable> findByVoteTrue();

    boolean existsByVoteTrue();

    // 주어진 시간과 겹치는 일정이 있는지 확인 (끝과 시작이 맞닿는 것은 겹침이 아님)
    // excludeId: 수정 시 자기 자신은 비교 대상에서 제외 (등록 시 null)
    @Query("""
        select count(t) > 0 from Timetable t
        where t.startTime < :endTime
          and t.endTime > :startTime
          and (:excludeId is null or t.id <> :excludeId)
        """)
    boolean existsOverlapping(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("excludeId") Long excludeId
    );
}
