package com.ganzithon.makers.platform.domain.vote.application;

import com.ganzithon.makers.platform.domain.vote.infrastructure.ProjectVoteCount;
import com.ganzithon.makers.platform.domain.vote.infrastructure.VoteRepository;
import java.util.Map;
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

  // 회원 삭제 시 그 회원의 투표도 삭제 (투표했던 프로젝트의 득표수가 줄어듦)
  @Transactional
  public void deleteAllByUserId(Long userId) {
    voteRepository.deleteAllByUserId(userId);
  }
}
