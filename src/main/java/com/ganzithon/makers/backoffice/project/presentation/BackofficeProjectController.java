package com.ganzithon.makers.backoffice.project.presentation;

import com.ganzithon.makers.backoffice.project.application.BackofficeProjectService;
import com.ganzithon.makers.backoffice.project.presentation.dto.ProjectListResponse;
import com.ganzithon.makers.common.dto.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// TODO: 관리자 권한 체크 필요
@Tag(name = "Backoffice - 프로젝트 관리")
@RestController
@RequestMapping("/api/backoffice/projects")
@RequiredArgsConstructor
public class BackofficeProjectController {

  private final BackofficeProjectService backofficeProjectService;

  @Operation(
      summary = "프로젝트 목록 조회/검색",
      description = "keyword 가 없으면 전체 목록, 있으면 프로젝트명에 keyword 가 포함된 프로젝트만 조회합니다. "
          + "득표수가 많은 순으로 조회하며, 득표수가 같으면 등록 순입니다. 표가 없는 프로젝트의 득표수는 0입니다.")
  @GetMapping
  public ResponseEntity<SuccessResponse<List<ProjectListResponse>>> getProjects(
      @Parameter(description = "프로젝트명 검색어 (일부 일치)", example = "반차")
      @RequestParam(required = false) String keyword
  ) {
    return ResponseEntity.ok(SuccessResponse.of(backofficeProjectService.getProjects(keyword)));
  }
}
