package com.ganzithon.makers.platform.domain.vote.application;

import com.ganzithon.makers.platform.domain.vote.infrastructure.ProjectVoteCount;
import com.ganzithon.makers.platform.domain.vote.domain.Vote;
import com.ganzithon.makers.platform.domain.vote.infrastructure.VoteRepository;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoteService {

  private final VoteRepository voteRepository;

  // key: projectId, value: 득표수 (표가 없는 프로젝트는 포함되지 않음)
  public Map<Long, Long> countVotesByProject() {
    return voteRepository.countGroupByProjectId().stream()
        .collect(Collectors.toMap(ProjectVoteCount::getProjectId, ProjectVoteCount::getVoteCount));
  }

  public long countByProjectId(Long projectId) {
    return voteRepository.countByProjectId(projectId);
  }

  public Set<Long> findVotedProjectIdsByUserId(Long userId) {
    return voteRepository.findAllByUserId(userId).stream()
        .map(Vote::getProjectId)
        .collect(Collectors.toSet());
  }
}
