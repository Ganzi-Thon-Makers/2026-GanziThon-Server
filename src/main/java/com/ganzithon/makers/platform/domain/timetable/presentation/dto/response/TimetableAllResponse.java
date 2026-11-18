package com.ganzithon.makers.platform.domain.timetable.presentation.dto.response;

import com.ganzithon.makers.platform.domain.timetable.application.dto.TimetableInfo;
import java.util.List;

public record TimetableAllResponse(
        List<Timetable> timetables
) {

    public record Timetable(
            Long id,
            String title,
            String startTime,
            String endTime
    ) {
        public static Timetable from(TimetableInfo info) {
            return new Timetable(
                    info.id(),
                    info.title(),
                    info.startTime(),
                    info.endTime()
            );
        }
    }

    public static TimetableAllResponse from(List<TimetableInfo> infos) {
        return new TimetableAllResponse(
                infos.stream()
                        .map(Timetable::from)
                        .toList()
        );
    }
}
