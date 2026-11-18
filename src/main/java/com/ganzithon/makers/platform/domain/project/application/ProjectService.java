package com.ganzithon.makers.platform.domain.project.application;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.project.domain.Project;
import com.ganzithon.makers.platform.domain.project.infrastructure.ProjectRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

  private final ProjectRepository projectRepository;

  public Project findById(Long projectId) {
    return projectRepository.findById(projectId)
        .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
  }

  public List<Project> findAll() {
    return projectRepository.findAll();
  }

  public List<Project> searchByTitle(String keyword) {
    return projectRepository.findAllByTitleContainingIgnoreCase(keyword);
  }

  // 팀 번호가 있는 프로젝트만, 팀 번호 순
  public List<Project> findAllWithTeamNumber() {
    return projectRepository.findAllByTeamNumberIsNotNullOrderByTeamNumberAsc();
  }

  public boolean existsByTeamNumber(Integer teamNumber) {
    return projectRepository.existsByTeamNumber(teamNumber);
  }

  // 수정 시 자기 자신은 제외하고 팀 번호 중복 확인
  public boolean existsByTeamNumberExcept(Integer teamNumber, Long projectId) {
    return projectRepository.existsByTeamNumberAndIdNot(teamNumber, projectId);
  }

  // team_number UNIQUE 위반을 호출한 쪽에서 바로 잡을 수 있도록 즉시 flush (수정된 엔티티도 이걸로 반영)
  @Transactional
  public Project save(Project project) {
    return projectRepository.saveAndFlush(project);
  }

  @Transactional
  public void delete(Project project) {
    projectRepository.delete(project);
  }

  public List<Project> findAllByIds(List<Long> projectIds) {
    return projectRepository.findAllById(projectIds);
  }
}
