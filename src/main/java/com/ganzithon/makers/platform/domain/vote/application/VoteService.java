package com.ganzithon.makers.platform.domain.vote.application;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.vote.domain.Vote;
import com.ganzithon.makers.platform.domain.vote.infrastructure.ProjectVoteCount;
import com.ganzithon.makers.platform.domain.vote.infrastructure.VoteRepository;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

  // 프로젝트 삭제 시 그 프로젝트가 받은 투표도 삭제 (투표했던 회원은 다시 투표 가능)
  @Transactional
  public void deleteAllByProjectId(Long projectId) {
    voteRepository.deleteAllByProjectId(projectId);
  }

  @Transactional
  public Vote cast(Long userId, Long projectId) {
    if (voteRepository.existsByUserId(userId)) {
      throw new BusinessException(ErrorCode.VOTE_ALREADY_CAST);
    }
    try {
      return voteRepository.save(Vote.create(userId, projectId));
    } catch (DataIntegrityViolationException e) {
      throw new BusinessException(ErrorCode.VOTE_ALREADY_CAST);
    }
  }

  public Set<Long> findVotedProjectIdsByUserId(Long userId) {
    return voteRepository.findAllByUserId(userId).stream()
        .map(Vote::getProjectId)
        .collect(Collectors.toSet());
  }
}
