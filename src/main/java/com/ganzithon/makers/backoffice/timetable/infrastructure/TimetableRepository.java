package com.ganzithon.makers.backoffice.timetable.infrastructure;

import com.ganzithon.makers.platform.domain.timetable.domain.Timetable;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {

  // 시간이 겹치는 일정이 있어도 순서가 항상 같도록 시작 → 종료 → id 순으로 정렬
  List<Timetable> findAllByOrderByStartTimeAscEndTimeAscIdAsc();

  boolean existsByVoteTrue();
}
