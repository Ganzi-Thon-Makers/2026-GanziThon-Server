package com.ganzithon.makers.platform.domain.vote.application;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.platform.domain.project.application.ProjectService;
import com.ganzithon.makers.platform.domain.timetable.domain.Timetable;
import com.ganzithon.makers.platform.domain.timetable.domain.VoteStatus;
import com.ganzithon.makers.platform.domain.timetable.infrastructure.TimetableRepository;
import com.ganzithon.makers.platform.domain.user.application.UserService;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.domain.User;
import com.ganzithon.makers.platform.domain.vote.presentation.dto.VotePeriodResponse;
import com.ganzithon.makers.platform.domain.vote.presentation.dto.VoteSubmitRequest;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoteFacade {

  private final VoteService voteService;
  private final UserService userService;
  private final ProjectService projectService;
  private final TimetableRepository timetableRepository;
  private final Clock clock;

  @Transactional
  public void submit(AuthPrincipal principal, VoteSubmitRequest request) {
    if (principal.role() != Role.PARTICIPANT) {
      throw new BusinessException(ErrorCode.VOTE_PARTICIPANT_ONLY);
    }

    User user = userService.findById(principal.userId());
    projectService.findById(request.projectId());

    if (request.projectId().equals(user.getProjectId())) {
      throw new BusinessException(ErrorCode.VOTE_OWN_PROJECT);
    }

    validateVoteInProgress();

    voteService.cast(user.getId(), request.projectId());
  }

  // 일정 미등록은 "투표 준비 중"으로 간주해 NOT_STARTED와 null 시간을 내려준다
  public VotePeriodResponse getPeriod() {
    LocalDateTime now = LocalDateTime.now(clock);
    return timetableRepository.findByVoteTrue()
        .map(timetable -> VotePeriodResponse.of(timetable, now))
        .orElseGet(() -> VotePeriodResponse.notStarted(now));
  }

  private void validateVoteInProgress() {
    VoteStatus status = timetableRepository.findByVoteTrue()
        .map(t -> t.voteStatusAt(LocalDateTime.now(clock)))
        .orElse(VoteStatus.NOT_STARTED);
    if (status != VoteStatus.IN_PROGRESS) {
      throw new BusinessException(ErrorCode.VOTE_NOT_IN_PROGRESS);
    }
  }
}
