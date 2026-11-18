package com.ganzithon.makers.platform.domain.vote.presentation;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.common.security.AuthUser;
import com.ganzithon.makers.platform.domain.vote.application.VoteFacade;
import com.ganzithon.makers.platform.domain.vote.presentation.dto.VoteSubmitRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/votes")
@RequiredArgsConstructor
public class VoteController implements VoteApi {

  private final VoteFacade voteFacade;

  @Override
  @PostMapping
  public ResponseEntity<SuccessResponse<Void>> submit(
      @AuthUser AuthPrincipal principal,
      @Valid @RequestBody VoteSubmitRequest request
  ) {
    voteFacade.submit(principal, request);
    return ResponseEntity.ok(SuccessResponse.of("투표가 완료되었습니다."));
  }
}
