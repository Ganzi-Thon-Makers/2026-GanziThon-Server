package com.ganzithon.makers.platform.domain.timetable.application.dto;

import com.ganzithon.makers.platform.domain.timetable.domain.Timetable;
import java.time.format.DateTimeFormatter;

public record TimetableInfo(
        Long id,
        String title,
        String description,
        String startTime,
        String endTime
) {
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    public static TimetableInfo from(Timetable timetable) {
        return new TimetableInfo(
                timetable.getId(),
                timetable.getTitle(),
                timetable.getDescription(),
                timetable.getStartTime().format(TIME_FORMATTER),
                timetable.getEndTime().format(TIME_FORMATTER)
        );
    }
}
