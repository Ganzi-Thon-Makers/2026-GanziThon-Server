package com.ganzithon.makers.platform.domain.vote.presentation.dto;

import com.ganzithon.makers.platform.domain.timetable.domain.Timetable;
import com.ganzithon.makers.platform.domain.timetable.domain.VoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record VotePeriodResponse(

    @Schema(description = "투표 상태 (NOT_STARTED: 시작 전, IN_PROGRESS: 진행 중, PAUSED: 일시중지, ENDED: 종료). "
        + "일정 미등록 시에도 NOT_STARTED로 내려갑니다", example = "IN_PROGRESS")
    VoteStatus status,

    @Schema(description = "투표 시작 시각. 일정 미등록 시 null", example = "2026-11-20T14:00:00", nullable = true)
    LocalDateTime startTime,

    @Schema(description = "투표 종료 시각. 일정 미등록 시 null", example = "2026-11-20T18:00:00", nullable = true)
    LocalDateTime endTime,

    @Schema(description = "서버 현재 시각. 카운트다운 등 클라이언트 시계 오차 방지용", example = "2026-11-20T15:30:00")
    LocalDateTime serverTime
) {

  public static VotePeriodResponse of(Timetable timetable, LocalDateTime now) {
    return new VotePeriodResponse(
        timetable.voteStatusAt(now),
        timetable.getStartTime(),
        timetable.getEndTime(),
        now
    );
  }

  public static VotePeriodResponse notStarted(LocalDateTime now) {
    return new VotePeriodResponse(VoteStatus.NOT_STARTED, null, null, now);
  }
}
