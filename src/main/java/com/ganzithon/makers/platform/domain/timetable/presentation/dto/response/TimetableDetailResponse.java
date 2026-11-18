package com.ganzithon.makers.platform.domain.timetable.presentation.dto.response;

import com.ganzithon.makers.platform.domain.timetable.application.dto.TimetableDetailInfo;

public record TimetableDetailResponse(
        Long id,
        String title,
        String description,
        String startTime,
        String endTime
) {
    public static TimetableDetailResponse from(TimetableDetailInfo info) {
        return new TimetableDetailResponse(
                info.id(),
                info.title(),
                info.description(),
                info.startTime(),
                info.endTime()
        );
    }
}
