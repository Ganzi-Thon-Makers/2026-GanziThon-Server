package com.ganzithon.makers.backoffice.vote.application;

import com.ganzithon.makers.backoffice.timetable.application.BackofficeTimetableService;
import com.ganzithon.makers.backoffice.vote.presentation.dto.VoteResponse;
import com.ganzithon.makers.backoffice.vote.presentation.dto.VoteTimeUpdateRequest;
import com.ganzithon.makers.platform.domain.timetable.domain.Timetable;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 투표 시간 조회/수정, 일시중지/재개 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BackofficeVoteService {

  private final BackofficeTimetableService backofficeTimetableService;
  private final Clock clock;

  public VoteResponse getVote() {
    return toResponse(backofficeTimetableService.getVoteTimetable());
  }

  @Transactional
  public VoteResponse updateTime(VoteTimeUpdateRequest request) {
    Timetable vote = backofficeTimetableService.changeVoteTime(request.startTime(), request.endTime());
    return toResponse(vote);
  }

  @Transactional
  public VoteResponse pause() {
    Timetable vote = backofficeTimetableService.getVoteTimetable();
    vote.pause();
    return toResponse(vote);
  }

  @Transactional
  public VoteResponse resume() {
    Timetable vote = backofficeTimetableService.getVoteTimetable();
    vote.resume();
    return toResponse(vote);
  }

  private VoteResponse toResponse(Timetable vote) {
    return VoteResponse.of(vote, vote.voteStatusAt(now()));
  }

  private LocalDateTime now() {
    return LocalDateTime.now(clock);
  }
}
