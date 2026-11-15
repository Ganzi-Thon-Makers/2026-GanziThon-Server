package com.ganzithon.makers.backoffice.timetable.presentation;

import com.ganzithon.makers.backoffice.timetable.application.BackofficeTimetableService;
import com.ganzithon.makers.backoffice.timetable.presentation.dto.TimetableCreateRequest;
import com.ganzithon.makers.backoffice.timetable.presentation.dto.TimetableResponse;
import com.ganzithon.makers.backoffice.timetable.presentation.dto.TimetableUpdateRequest;
import com.ganzithon.makers.common.dto.SuccessResponse;
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
@RestController
@RequestMapping("/api/backoffice/timetables")
@RequiredArgsConstructor
public class BackofficeTimetableController {

  private final BackofficeTimetableService backofficeTimetableService;

  @PostMapping
  public ResponseEntity<SuccessResponse<TimetableResponse>> create(
      @Valid @RequestBody TimetableCreateRequest request
  ) {
    TimetableResponse response = TimetableResponse.of(backofficeTimetableService.create(request));
    return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.of(response));
  }

  @GetMapping
  public ResponseEntity<SuccessResponse<List<TimetableResponse>>> getTimetables() {
    List<TimetableResponse> responses = backofficeTimetableService.findAllOrdered().stream()
        .map(TimetableResponse::of)
        .toList();
    return ResponseEntity.ok(SuccessResponse.of(responses));
  }

  @PutMapping("/{timetableId}")
  public ResponseEntity<SuccessResponse<TimetableResponse>> update(
      @PathVariable Long timetableId,
      @Valid @RequestBody TimetableUpdateRequest request
  ) {
    TimetableResponse response =
        TimetableResponse.of(backofficeTimetableService.update(timetableId, request));
    return ResponseEntity.ok(SuccessResponse.of(response));
  }

  @DeleteMapping("/{timetableId}")
  public ResponseEntity<SuccessResponse<Void>> delete(@PathVariable Long timetableId) {
    backofficeTimetableService.delete(timetableId);
    return ResponseEntity.ok(SuccessResponse.of("삭제되었습니다."));
  }
}
