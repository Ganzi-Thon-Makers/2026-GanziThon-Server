package com.ganzithon.makers.platform.domain.timetable.application.dto;

import com.ganzithon.makers.platform.domain.timetable.domain.Timetable;
import java.time.format.DateTimeFormatter;

public record TimetableDetailInfo(
        Long id,
        String title,
        String description,
        String startTime,
        String endTime
) {
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    public static TimetableDetailInfo from(Timetable timetable) {
        return new TimetableDetailInfo(
                timetable.getId(),
                timetable.getTitle(),
                timetable.getDescription(),
                timetable.getStartTime().format(TIME_FORMATTER),
                timetable.getEndTime().format(TIME_FORMATTER)
        );
    }
}
