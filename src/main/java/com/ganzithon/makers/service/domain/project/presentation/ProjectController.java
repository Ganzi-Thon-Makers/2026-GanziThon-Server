package com.ganzithon.makers.service.domain.project.presentation;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.service.domain.booth.domain.Zone;
import com.ganzithon.makers.service.domain.project.application.ProjectBoothQueryFacade;
import com.ganzithon.makers.service.domain.project.presentation.dto.ProjectDetailResponse;
import com.ganzithon.makers.service.domain.project.presentation.dto.ProjectSummaryResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

  private final ProjectBoothQueryFacade projectBoothQueryFacade;

  @GetMapping
  public ResponseEntity<SuccessResponse<Map<Zone, List<ProjectSummaryResponse>>>> getProjects() {
    return ResponseEntity.ok(SuccessResponse.of(projectBoothQueryFacade.getProjectsGroupedByZone()));
  }

  @GetMapping("/{projectId}")
  public ResponseEntity<SuccessResponse<ProjectDetailResponse>> getProjectDetail(@PathVariable Long projectId) {
    return ResponseEntity.ok(SuccessResponse.of(projectBoothQueryFacade.getProjectDetail(projectId)));
  }
}
