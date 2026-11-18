package com.ganzithon.makers.platform.domain.booth.application;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.booth.application.dto.BoothDetailInfo;
import com.ganzithon.makers.platform.domain.booth.application.dto.BoothInfo;
import com.ganzithon.makers.platform.domain.booth.domain.Booth;
import com.ganzithon.makers.platform.domain.booth.infrastructure.BoothRepository;
import java.util.List;

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
    List<BoothInfo> getBooths =  boothRepository.findAll()
            .stream()
            .map(BoothInfo::from)
            .toList();
    return BoothListResponse.from(getBooths);
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
}
