package com.ganzithon.makers.backoffice.booth.presentation;

import com.ganzithon.makers.backoffice.booth.application.BackofficeBoothService;
import com.ganzithon.makers.backoffice.booth.presentation.dto.BoothAssignResponse;
import com.ganzithon.makers.backoffice.booth.presentation.dto.BoothProjectAssignRequest;
import com.ganzithon.makers.backoffice.booth.presentation.dto.BoothProjectResponse;
import com.ganzithon.makers.backoffice.booth.presentation.dto.BoothZoneResponse;
import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.platform.domain.booth.domain.Zone;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Backoffice - 부스 관리", description = "프로젝트(팀) 배정 / 교체 / 취소")
@RestController
@RequestMapping("/backoffice/api/v1/booths")
@RequiredArgsConstructor
public class BackofficeBoothController {

  private final BackofficeBoothService backofficeBoothService;

  @Operation(
      summary = "부스 현황 조회",
      description = "구역별로 호수 단위로 묶어 조회합니다. 구역 → 호수 → 부스 번호 순입니다. "
          + "project 는 배정된 프로젝트(팀)이며, 비어 있으면 null 입니다. zone 을 생략하면 전체 구역을 조회합니다.")
  @GetMapping
  public ResponseEntity<SuccessResponse<List<BoothZoneResponse>>> getBooths(
      @Parameter(description = "구역 (생략 시 전체)", example = "ZONE1")
      @RequestParam(required = false) Zone zone
  ) {
    return ResponseEntity.ok(SuccessResponse.of(backofficeBoothService.getBooths(zone)));
  }

  @Operation(
      summary = "배정 가능한 프로젝트 조회",
      description = "배정 드롭다운용입니다. 아직 부스가 없는 프로젝트와, boothId 를 주면 그 부스에 현재 배정된 프로젝트를 "
          + "팀 번호 순으로 조회합니다. 팀 번호가 없는 프로젝트는 맨 뒤입니다. 없는 boothId 면 404입니다.")
  @GetMapping("/assignable-projects")
  public ResponseEntity<SuccessResponse<List<BoothProjectResponse>>> getAssignableProjects(
      @Parameter(description = "현재 배정된 프로젝트도 함께 보여줄 부스 id (선택)", example = "1")
      @RequestParam(required = false) Long boothId
  ) {
    return ResponseEntity.ok(SuccessResponse.of(backofficeBoothService.getAssignableProjects(boothId)));
  }

  @Operation(
      summary = "부스에 프로젝트 배정 / 교체",
      description = "부스에 프로젝트(팀)를 배정합니다. 다른 프로젝트가 배정되어 있으면 교체되고, "
          + "같은 프로젝트를 다시 배정하면 그대로 성공합니다. "
          + "부스나 프로젝트가 없으면 404, 이미 다른 부스에 배정된 프로젝트면 409입니다.")
  @PutMapping("/{boothId}/project")
  public ResponseEntity<SuccessResponse<BoothAssignResponse>> assignProject(
      @PathVariable Long boothId,
      @Valid @RequestBody BoothProjectAssignRequest request
  ) {
    return ResponseEntity.ok(SuccessResponse.of(backofficeBoothService.assignProject(boothId, request.projectId())));
  }

  @Operation(
      summary = "부스 배정 취소",
      description = "부스의 프로젝트 배정을 비웁니다. 이미 비어 있어도 성공합니다. 부스가 없으면 404입니다.")
  @DeleteMapping("/{boothId}/project")
  public ResponseEntity<SuccessResponse<Void>> unassignProject(@PathVariable Long boothId) {
    backofficeBoothService.unassignProject(boothId);
    return ResponseEntity.ok(SuccessResponse.of("배정이 취소되었습니다."));
  }
}
