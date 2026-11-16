package com.ganzithon.makers.backoffice.timetable.presentation;

import com.ganzithon.makers.backoffice.timetable.application.BackofficeTimetableService;
import com.ganzithon.makers.backoffice.timetable.presentation.dto.TimetableCreateRequest;
import com.ganzithon.makers.backoffice.timetable.presentation.dto.TimetableResponse;
import com.ganzithon.makers.backoffice.timetable.presentation.dto.TimetableUpdateRequest;
import com.ganzithon.makers.common.dto.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: SUPER_ADMIN 전용 체크 구현 필요
@Tag(name = "Backoffice - 타임테이블 관리", description = "슈퍼 어드민 전용")
@RestController
@RequestMapping("/api/backoffice/timetables")
@RequiredArgsConstructor
public class BackofficeTimetableController {

  private final BackofficeTimetableService backofficeTimetableService;

  @Operation(
      summary = "일정 등록",
      description = "종료시간은 시작시간보다 늦어야 합니다. 다른 일정과 시간이 겹칠 수 없습니다. (끝과 시작이 맞닿는 것은 가능) "
          + "투표 일정(isVote: true)은 하나만 등록할 수 있습니다.")
  @PostMapping
  public ResponseEntity<SuccessResponse<TimetableResponse>> create(
      @Valid @RequestBody TimetableCreateRequest request
  ) {
    TimetableResponse response = TimetableResponse.of(backofficeTimetableService.create(request));
    return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.of(response));
  }

  @Operation(summary = "일정 목록 조회", description = "시작시간, 종료시간, id 순으로 정렬합니다.")
  @GetMapping
  public ResponseEntity<SuccessResponse<List<TimetableResponse>>> getTimetables() {
    List<TimetableResponse> responses = backofficeTimetableService.findAllOrdered().stream()
        .map(TimetableResponse::of)
        .toList();
    return ResponseEntity.ok(SuccessResponse.of(responses));
  }

  @Operation(
      summary = "일정 수정",
      description = "제목, 상세 내용, 시작/종료시간을 전체 교체합니다. 다른 일정과 시간이 겹칠 수 없으며, 투표 일정 여부는 수정할 수 없습니다.")
  @PutMapping("/{timetableId}")
  public ResponseEntity<SuccessResponse<TimetableResponse>> update(
      @PathVariable Long timetableId,
      @Valid @RequestBody TimetableUpdateRequest request
  ) {
    TimetableResponse response =
        TimetableResponse.of(backofficeTimetableService.update(timetableId, request));
    return ResponseEntity.ok(SuccessResponse.of(response));
  }

  @Operation(summary = "일정 삭제", description = "투표 일정은 삭제할 수 없습니다.")
  @DeleteMapping("/{timetableId}")
  public ResponseEntity<SuccessResponse<Void>> delete(@PathVariable Long timetableId) {
    backofficeTimetableService.delete(timetableId);
    return ResponseEntity.ok(SuccessResponse.of("삭제되었습니다."));
  }
}
