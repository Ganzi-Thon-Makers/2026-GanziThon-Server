package com.ganzithon.makers.backoffice.vote.presentation;

import com.ganzithon.makers.backoffice.vote.application.BackofficeVoteService;
import com.ganzithon.makers.backoffice.vote.presentation.dto.VoteResponse;
import com.ganzithon.makers.backoffice.vote.presentation.dto.VoteTimeUpdateRequest;
import com.ganzithon.makers.common.dto.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: SUPER_ADMIN 전용 체크 필요
@RestController
@RequestMapping("/api/backoffice/vote")
@RequiredArgsConstructor
public class BackofficeVoteController {

  private final BackofficeVoteService backofficeVoteService;

  @GetMapping
  public ResponseEntity<SuccessResponse<VoteResponse>> getVote() {
    return ResponseEntity.ok(SuccessResponse.of(backofficeVoteService.getVote()));
  }

  @PutMapping("/time")
  public ResponseEntity<SuccessResponse<VoteResponse>> updateTime(
      @Valid @RequestBody VoteTimeUpdateRequest request
  ) {
    return ResponseEntity.ok(SuccessResponse.of(backofficeVoteService.updateTime(request)));
  }

  @PostMapping("/pause")
  public ResponseEntity<SuccessResponse<VoteResponse>> pause() {
    return ResponseEntity.ok(SuccessResponse.of(backofficeVoteService.pause()));
  }

  @PostMapping("/resume")
  public ResponseEntity<SuccessResponse<VoteResponse>> resume() {
    return ResponseEntity.ok(SuccessResponse.of(backofficeVoteService.resume()));
  }
}
