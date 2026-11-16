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

  public List<Project> findAllByIds(List<Long> projectIds) {
    return projectRepository.findAllById(projectIds);
  }
}
