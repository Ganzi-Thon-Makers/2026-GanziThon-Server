package com.ganzithon.makers.backoffice.project.application;

import com.ganzithon.makers.backoffice.project.presentation.dto.ProjectListResponse;
import com.ganzithon.makers.platform.domain.project.application.ProjectService;
import com.ganzithon.makers.platform.domain.vote.application.VoteService;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 프로젝트 목록 조회 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BackofficeProjectService {

  private final ProjectService projectService;
  private final VoteService voteService;

  public List<ProjectListResponse> getProjects() {
    Map<Long, Long> voteCounts = voteService.countVotesByProject();
    return projectService.findAll().stream()
        .map(project -> ProjectListResponse.of(project, voteCounts.getOrDefault(project.getId(), 0L)))
        // 득표수 많은 순, 같으면 등록 순(id 오름차순)
        .sorted(Comparator.comparingLong(ProjectListResponse::voteCount).reversed()
            .thenComparing(ProjectListResponse::id))
        .toList();
  }
}
