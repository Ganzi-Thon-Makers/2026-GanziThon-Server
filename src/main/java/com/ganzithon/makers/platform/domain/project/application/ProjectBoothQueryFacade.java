package com.ganzithon.makers.platform.domain.project.application;

import com.ganzithon.makers.platform.domain.booth.application.BoothService;
import com.ganzithon.makers.platform.domain.booth.domain.Booth;
import com.ganzithon.makers.platform.domain.booth.domain.Zone;
import com.ganzithon.makers.platform.domain.project.domain.Project;
import com.ganzithon.makers.platform.domain.project.presentation.dto.ProjectDetailResponse;
import com.ganzithon.makers.platform.domain.project.presentation.dto.ProjectSummaryResponse;
import com.ganzithon.makers.platform.domain.user.application.UserService;
import com.ganzithon.makers.platform.domain.user.domain.Part;
import com.ganzithon.makers.platform.domain.user.domain.User;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectBoothQueryFacade {

  private final ProjectService projectService;
  private final BoothService boothService;
  private final UserService userService;

  public Map<Zone, List<ProjectSummaryResponse>> getProjectsGroupedByZone() {
    List<Booth> booths = boothService.findAllOrderByBoothNumber();

    List<Long> projectIds = booths.stream()
        .map(Booth::getProjectId)
        .toList();

    Map<Long, Project> projectsById = projectService.findAllByIds(projectIds).stream()
        .collect(Collectors.toMap(Project::getId, project -> project));

    Map<Zone, List<ProjectSummaryResponse>> result = new LinkedHashMap<>();
    for (Zone zone : Zone.values()) {
      result.put(zone, new ArrayList<>());
    }

    for (Booth booth : booths) {
      Project project = projectsById.get(booth.getProjectId());
      if (project == null) {
        continue;
      }
      result.get(booth.getZone()).add(ProjectSummaryResponse.of(project));
    }

    return result;
  }

  public ProjectDetailResponse getProjectDetail(Long projectId) {
    Project project = projectService.findById(projectId);
    Booth booth = boothService.findByProjectId(projectId);
    Map<Part, List<String>> members = getMembersGroupedByPart(projectId);
    return ProjectDetailResponse.of(project, booth, members);
  }

  private Map<Part, List<String>> getMembersGroupedByPart(Long projectId) {
    List<User> members = userService.findAllByProjectId(projectId);

    Map<Part, List<String>> result = new LinkedHashMap<>();
    for (Part part : Part.values()) {
      result.put(part, new ArrayList<>());
    }

    for (User member : members) {
      if (member.getPart() == null) {
        continue;
      }
      result.get(member.getPart()).add(member.getName());
    }

    return result;
  }
}
