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
import com.ganzithon.makers.platform.domain.vote.application.VoteService;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
  private final VoteService voteService;

  public Map<Zone, List<ProjectSummaryResponse>> getProjectsGroupedByZone(Long userId) {
    List<Booth> booths = boothService.findAllOrderByBoothNumber();

    Set<Long> votedProjectIds = userId != null
        ? voteService.findVotedProjectIdsByUserId(userId)
        : Set.of();

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
      result.get(booth.getZone())
          .add(ProjectSummaryResponse.of(project, votedProjectIds.contains(project.getId())));
    }

    return result;
  }

  public ProjectDetailResponse getProjectDetail(Long projectId) {
    Project project = projectService.findById(projectId);
    Booth booth = boothService.findByProjectId(projectId);
    List<ProjectDetailResponse.PartMembers> members = getMembersGroupedByPart(projectId);
    return ProjectDetailResponse.of(project, booth, members);
  }

  private List<ProjectDetailResponse.PartMembers> getMembersGroupedByPart(Long projectId) {
    List<User> members = userService.findAllByProjectId(projectId);

    Map<Part, List<String>> grouped = new EnumMap<>(Part.class);
    for (User member : members) {
      if (member.getPart() == null) {
        continue;
      }
      grouped.computeIfAbsent(member.getPart(), part -> new ArrayList<>()).add(member.getName());
    }

    List<ProjectDetailResponse.PartMembers> result = new ArrayList<>();
    for (Part part : Part.values()) {
      List<String> names = grouped.get(part);
      if (names != null) {
        result.add(new ProjectDetailResponse.PartMembers(part, names));
      }
    }
    return result;
  }
}
