package com.ganzithon.makers.backoffice.project.presentation;

import com.ganzithon.makers.backoffice.project.application.BackofficeProjectService;
import com.ganzithon.makers.backoffice.project.presentation.dto.ProjectCreateRequest;
import com.ganzithon.makers.backoffice.project.presentation.dto.ProjectDetailResponse;
import com.ganzithon.makers.backoffice.project.presentation.dto.ProjectListResponse;
import com.ganzithon.makers.backoffice.project.presentation.dto.ProjectUpdateRequest;
import com.ganzithon.makers.backoffice.project.presentation.dto.TeamResponse;
import com.ganzithon.makers.common.dto.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Backoffice - 프로젝트 관리")
@RestController
@RequestMapping("/backoffice/api/v1/projects")
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

  @Operation(
      summary = "팀 목록 조회",
      description = "팀 번호 선택용으로 팀 번호, 팀명, 프로젝트 id 만 팀 번호 순으로 조회합니다. 팀 번호가 없는 프로젝트는 제외됩니다.")
  @GetMapping("/teams")
  public ResponseEntity<SuccessResponse<List<TeamResponse>>> getTeams() {
    return ResponseEntity.ok(SuccessResponse.of(backofficeProjectService.getTeams()));
  }

  @Operation(
      summary = "프로젝트 상세 조회",
      description = "팀원은 파트 순(PLAN → DESIGN → WEB → APP → SERVER)으로 정렬됩니다. 득표수는 조회만 가능합니다.")
  @GetMapping("/{projectId}")
  public ResponseEntity<SuccessResponse<ProjectDetailResponse>> getProject(@PathVariable Long projectId) {
    return ResponseEntity.ok(SuccessResponse.of(backofficeProjectService.getProject(projectId)));
  }

  @Operation(
      summary = "프로젝트 등록",
      description = "multipart/form-data 로 request와 대표 이미지 파일을 함께 보냅니다. "
          + "memberIds 에는 팀원(회원 id) 최종 목록을 보내며, 이미 다른 프로젝트에 소속된 회원은 등록할 수 없습니다.")
  @RequestBody(content = @Content(
      mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
      encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)))
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<SuccessResponse<ProjectDetailResponse>> create(
      @Valid @RequestPart("request") ProjectCreateRequest request,
      @RequestPart("thumbnail") MultipartFile thumbnail
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(SuccessResponse.of(backofficeProjectService.create(request, thumbnail)));
  }

  @Operation(
      summary = "프로젝트 수정",
      description = "multipart/form-data 로 request와 대표 이미지 파일을 함께 보냅니다. "
          + "대표 이미지를 바꾸지 않으면 thumbnail 은 보내지 않아도 됩니다. "
          + "memberIds 에는 팀원(회원 id) 최종 목록을 보내며, 목록에서 빠진 팀원은 프로젝트에서 연결 해제됩니다. "
          + "득표수는 수정할 수 없습니다.")
  @RequestBody(content = @Content(
      mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
      encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)))
  @PutMapping(value = "/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<SuccessResponse<ProjectDetailResponse>> update(
      @PathVariable Long projectId,
      @Valid @RequestPart("request") ProjectUpdateRequest request,
      @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
  ) {
    return ResponseEntity.ok(SuccessResponse.of(backofficeProjectService.update(projectId, request, thumbnail)));
  }

  @Operation(
      summary = "프로젝트 삭제",
      description = "되돌릴 수 없습니다. 팀원과 부스는 연결만 해제되고, 프로젝트가 받은 투표는 삭제됩니다. "
          + "(투표했던 참가자는 다시 투표할 수 있습니다)")
  @DeleteMapping("/{projectId}")
  public ResponseEntity<SuccessResponse<Void>> delete(@PathVariable Long projectId) {
    backofficeProjectService.delete(projectId);
    return ResponseEntity.ok(SuccessResponse.of("삭제되었습니다."));
  }
}
