package com.ganzithon.makers.platform.domain.vote.presentation.dto;

import com.ganzithon.makers.platform.domain.timetable.domain.Timetable;
import com.ganzithon.makers.platform.domain.timetable.domain.VoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record VotePeriodResponse(

    @Schema(description = "투표 상태 (NOT_STARTED: 시작 전, IN_PROGRESS: 진행 중, PAUSED: 일시중지, ENDED: 종료)", example = "IN_PROGRESS")
    VoteStatus status,

    @Schema(description = "투표 시작 시각", example = "2026-11-20T14:00:00")
    LocalDateTime startTime,

    @Schema(description = "투표 종료 시각", example = "2026-11-20T18:00:00")
    LocalDateTime endTime
) {

  public static VotePeriodResponse of(Timetable timetable, LocalDateTime now) {
    return new VotePeriodResponse(
        timetable.voteStatusAt(now),
        timetable.getStartTime(),
        timetable.getEndTime()
    );
  }
}
