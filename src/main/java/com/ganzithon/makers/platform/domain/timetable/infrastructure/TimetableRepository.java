package com.ganzithon.makers.platform.domain.timetable.infrastructure;

import com.ganzithon.makers.platform.domain.timetable.domain.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {
}
