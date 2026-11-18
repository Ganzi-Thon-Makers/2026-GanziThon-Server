package com.ganzithon.makers.backoffice.booth.application;

import com.ganzithon.makers.backoffice.booth.presentation.dto.BoothAssignResponse;
import com.ganzithon.makers.backoffice.booth.presentation.dto.BoothProjectResponse;
import com.ganzithon.makers.backoffice.booth.presentation.dto.BoothZoneResponse;
import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.booth.application.BoothService;
import com.ganzithon.makers.platform.domain.booth.domain.Booth;
import com.ganzithon.makers.platform.domain.booth.domain.Zone;
import com.ganzithon.makers.platform.domain.project.application.ProjectService;
import com.ganzithon.makers.platform.domain.project.domain.Project;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 부스 현황 조회, 부스에 프로젝트(팀) 배정 / 교체 / 취소. 부스 생성·삭제·정보 수정은 없음 (고정 배치) */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BackofficeBoothService {

  private final BoothService boothService;
  private final ProjectService projectService;

  // 구역 → 호수 → 부스 번호 순. 부스와 프로젝트를 한 번씩 조회해 메모리에서 합침 (N+1 방지)
  public List<BoothZoneResponse> getBooths(Zone zone) {
    List<Booth> booths = boothService.findAllByZone(zone);
    Map<Long, Project> projects = findProjects(booths.stream()
        .map(Booth::getProjectId)
        .filter(Objects::nonNull)
        .distinct()
        .toList());

    Map<Zone, List<Booth>> byZone = booths.stream()
        .sorted(Comparator.comparing(Booth::getZone).thenComparing(Booth::getBoothNumber))
        .collect(Collectors.groupingBy(Booth::getZone, LinkedHashMap::new, Collectors.toList()));

    List<BoothZoneResponse> result = new ArrayList<>();
    byZone.forEach((z, zoneBooths) -> {
      // 호수는 그 호수의 가장 작은 부스 번호 순 (문자열 정렬 시 "10번" 이 "2번" 앞에 오는 문제 방지)
      Map<String, List<BoothZoneResponse.BoothItem>> byRoom = new LinkedHashMap<>();
      for (Booth booth : zoneBooths) {
        byRoom.computeIfAbsent(booth.getRoomNumber(), r -> new ArrayList<>())
            .add(new BoothZoneResponse.BoothItem(
                booth.getId(),
                booth.getBoothNumber(),
                BoothProjectResponse.of(booth.getProjectId() == null ? null : projects.get(booth.getProjectId()))));
      }
      List<BoothZoneResponse.Room> rooms = byRoom.entrySet().stream()
          .map(e -> new BoothZoneResponse.Room(e.getKey(), e.getValue()))
          .toList();
      int assignedCount = (int) zoneBooths.stream().filter(b -> b.getProjectId() != null).count();
      result.add(new BoothZoneResponse(z, assignedCount, zoneBooths.size(), rooms));
    });
    return result;
  }

  // 배정 드롭다운용: 아직 부스가 없는 프로젝트 + (boothId 가 있으면) 그 부스에 현재 배정된 프로젝트. 팀 번호 순
  public List<BoothProjectResponse> getAssignableProjects(Long boothId) {
    Long currentProjectId = boothId == null ? null : boothService.findById(boothId).getProjectId();
    Set<Long> assigned = new HashSet<>(boothService.findAssignedProjectIds());

    return projectService.findAll().stream()
        .filter(p -> !assigned.contains(p.getId()) || p.getId().equals(currentProjectId))
        .sorted(Comparator.comparing(Project::getTeamNumber, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Project::getId))
        .map(BoothProjectResponse::of)
        .toList();
  }

  // 배정 또는 교체. 같은 프로젝트를 다시 배정하면 그대로 성공
  @Transactional
  public BoothAssignResponse assignProject(Long boothId, Long projectId) {
    Booth booth = boothService.findById(boothId);
    Project project = projectService.findById(projectId);

    boolean assignedElsewhere = boothService.findAssignedProjectIds().contains(projectId)
        && !projectId.equals(booth.getProjectId());
    if (assignedElsewhere) {
      throw new BusinessException(ErrorCode.PROJECT_ALREADY_ASSIGNED_TO_BOOTH);
    }

    booth.assignProject(projectId);
    try {
      boothService.saveAndFlush(booth);
    } catch (DataIntegrityViolationException e) {
      // 동시 요청으로 위 확인을 함께 통과한 경우, DB UNIQUE 제약(project_id)이 막는다
      throw new BusinessException(ErrorCode.PROJECT_ALREADY_ASSIGNED_TO_BOOTH);
    }
    return BoothAssignResponse.of(booth, project);
  }

  // 배정 취소. 이미 비어 있어도 성공
  @Transactional
  public void unassignProject(Long boothId) {
    Booth booth = boothService.findById(boothId);
    booth.clearProject();
  }

  private Map<Long, Project> findProjects(List<Long> projectIds) {
    if (projectIds.isEmpty()) {
      return Map.of();
    }
    return projectService.findAllByIds(projectIds).stream()
        .collect(Collectors.toMap(Project::getId, Function.identity()));
  }
}
