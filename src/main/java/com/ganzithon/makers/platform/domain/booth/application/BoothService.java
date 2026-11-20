package com.ganzithon.makers.platform.domain.booth.application;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.booth.application.dto.BoothDetailInfo;
import com.ganzithon.makers.platform.domain.booth.application.dto.BoothInfo;
import com.ganzithon.makers.platform.domain.booth.domain.Booth;
import com.ganzithon.makers.platform.domain.booth.domain.Zone;
import com.ganzithon.makers.platform.domain.booth.infrastructure.BoothRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ganzithon.makers.platform.domain.booth.presentation.response.BoothDetailResponse;
import com.ganzithon.makers.platform.domain.booth.presentation.response.BoothListResponse;
import com.ganzithon.makers.platform.domain.project.domain.Project;
import com.ganzithon.makers.platform.domain.project.infrastructure.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoothService {

  private final BoothRepository boothRepository;
  private final ProjectRepository projectRepository;

  //부스 전체 조회
  public BoothListResponse getBoothsAll() {

    List<Booth> booths = boothRepository.findAll();

    List<Long> projectIds = booths.stream()
            .map(Booth::getProjectId)
            .filter(Objects::nonNull)
            .toList();

    // toMap은 value가 null이면 NPE라서, 팀 번호 미정 프로젝트를 허용하는 HashMap으로 수집
    Map<Long, Integer> teamNumberByProjectId = new HashMap<>();
    projectRepository.findAllById(projectIds)
            .forEach(p -> teamNumberByProjectId.put(p.getId(), p.getTeamNumber()));


    List<BoothInfo> boothInfos = booths.stream()
            .map(booth -> BoothInfo.of(
                    booth,
                    teamNumberByProjectId.get(booth.getProjectId())))   // 미배정이면 null
            .toList();

    return BoothListResponse.from(boothInfos);
  }

  //부스 상세 조회
  public BoothDetailResponse getBoothDetail(Long boothId) {
    Booth booth = boothRepository.findById(boothId)
            .orElseThrow(() ->
                new BusinessException(ErrorCode.BOOTH_NOT_FOUND)
            );

    Long projectId = booth.getProjectId();

    if(projectId == null) {
      throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
    }

    Project project = projectRepository.findById(projectId)
            .orElseThrow(() ->
                    new BusinessException(ErrorCode.PROJECT_NOT_FOUND)
            );
    BoothDetailInfo boothDetails = BoothDetailInfo.from(project);
    return BoothDetailResponse.from(boothDetails);
  }


  public List<Booth> findAllOrderByBoothNumber() {
    return boothRepository.findAllByOrderByBoothNumberAsc();
  }

  public Booth findById(Long boothId) {
    return boothRepository.findById(boothId)
        .orElseThrow(() -> new BusinessException(ErrorCode.BOOTH_NOT_FOUND));
  }

  public Booth findByProjectId(Long projectId) {
    return boothRepository.findByProjectId(projectId)
        .orElseThrow(() -> new BusinessException(ErrorCode.BOOTH_NOT_FOUND));
  }

  // 프로젝트 삭제 시 부스는 유지하고 연결만 해제
  @Transactional
  public void clearProject(Long projectId) {
    boothRepository.clearProject(projectId);
  }

  public List<Booth> findAllByIds(List<Long> boothIds) {
    return boothRepository.findAllById(boothIds);
  }

  // zone 이 null 이면 전체. 구역 → 부스 번호 순
  public List<Booth> findAllByZone(Zone zone) {
    return zone == null
        ? boothRepository.findAllByOrderByZoneAscBoothNumberAsc()
        : boothRepository.findAllByZoneOrderByBoothNumberAsc(zone);
  }

  // 같은 구역에 같은 부스 번호가 (자기 자신 제외) 이미 있는지
  public boolean existsByZoneAndBoothNumberExcept(Zone zone, Integer boothNumber, Long boothId) {
    return boothRepository.existsByZoneAndBoothNumberAndIdNot(zone, boothNumber, boothId);
  }

  // 부스가 배정된 프로젝트 id 목록
  public List<Long> findAssignedProjectIds() {
    return boothRepository.findAssignedProjectIds();
  }

  // project_id UNIQUE 위반을 호출한 쪽에서 바로 잡을 수 있도록 즉시 flush
  @Transactional
  public Booth saveAndFlush(Booth booth) {
    return boothRepository.saveAndFlush(booth);
  }
}
