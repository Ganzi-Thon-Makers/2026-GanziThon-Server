package com.ganzithon.makers.backoffice.vote.presentation;

import com.ganzithon.makers.backoffice.vote.application.BackofficeVoteService;
import com.ganzithon.makers.backoffice.vote.presentation.dto.VoteResponse;
import com.ganzithon.makers.backoffice.vote.presentation.dto.VoteTimeUpdateRequest;
import com.ganzithon.makers.common.dto.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Backoffice - 투표 관리", description = "슈퍼 어드민 전용")
@RestController
@RequestMapping("/backoffice/api/v1/vote")
@RequiredArgsConstructor
public class BackofficeVoteController {

  private final BackofficeVoteService backofficeVoteService;

  @Operation(
      summary = "투표 시간/상태 조회",
      description = "status: NOT_STARTED(시작 전), IN_PROGRESS(진행 중), PAUSED(일시중지), ENDED(종료)")
  @GetMapping
  public ResponseEntity<SuccessResponse<VoteResponse>> getVote() {
    return ResponseEntity.ok(SuccessResponse.of(backofficeVoteService.getVote()));
  }

  @Operation(
      summary = "투표 시간 수정",
      description = "마감시간은 시작시간보다 늦어야 하며, 다른 일정과 시간이 겹칠 수 없습니다. "
          + "진행 중에는 수정할 수 없으며, 일시중지 후 수정할 수 있습니다.")
  @PutMapping("/time")
  public ResponseEntity<SuccessResponse<VoteResponse>> updateTime(
      @Valid @RequestBody VoteTimeUpdateRequest request
  ) {
    return ResponseEntity.ok(SuccessResponse.of(backofficeVoteService.updateTime(request)));
  }

  @Operation(summary = "투표 일시중지", description = "일시중지해도 마감시간은 늘어나지 않습니다.")
  @PostMapping("/pause")
  public ResponseEntity<SuccessResponse<VoteResponse>> pause() {
    return ResponseEntity.ok(SuccessResponse.of(backofficeVoteService.pause()));
  }

  @Operation(summary = "투표 재개")
  @PostMapping("/resume")
  public ResponseEntity<SuccessResponse<VoteResponse>> resume() {
    return ResponseEntity.ok(SuccessResponse.of(backofficeVoteService.resume()));
  }
}
