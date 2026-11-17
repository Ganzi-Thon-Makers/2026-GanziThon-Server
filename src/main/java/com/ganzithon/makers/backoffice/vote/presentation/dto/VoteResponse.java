package com.ganzithon.makers.backoffice.vote.presentation.dto;

import com.ganzithon.makers.platform.domain.timetable.domain.Timetable;
import com.ganzithon.makers.platform.domain.timetable.domain.VoteStatus;
import java.time.LocalDateTime;

public record VoteResponse(
    LocalDateTime startTime,
    LocalDateTime endTime,
    boolean paused,
    VoteStatus status
) {

  public static VoteResponse of(Timetable vote, VoteStatus status) {
    return new VoteResponse(
        vote.getStartTime(),
        vote.getEndTime(),
        vote.isPaused(),
        status
    );
  }
}
