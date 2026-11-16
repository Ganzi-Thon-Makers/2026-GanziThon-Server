package com.ganzithon.makers.backoffice.timetable.presentation.dto;

import com.ganzithon.makers.platform.domain.timetable.domain.Timetable;
import java.time.LocalDateTime;

public record TimetableResponse(
    Long id,
    String title,
    String description,
    LocalDateTime startTime,
    LocalDateTime endTime
) {

  public static TimetableResponse of(Timetable timetable) {
    return new TimetableResponse(
        timetable.getId(),
        timetable.getTitle(),
        timetable.getDescription(),
        timetable.getStartTime(),
        timetable.getEndTime()
    );
  }
}
