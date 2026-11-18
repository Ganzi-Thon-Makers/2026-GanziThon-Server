package com.ganzithon.makers.platform.domain.vote.presentation;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.common.security.AuthUser;
import com.ganzithon.makers.platform.domain.vote.application.VoteFacade;
import com.ganzithon.makers.platform.domain.vote.presentation.dto.VoteSubmitRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Vote", description = "프로젝트 인기투표")
@RestController
@RequestMapping("/api/v1/votes")
@RequiredArgsConstructor
public class VoteController {

  private final VoteFacade voteFacade;

  @Operation(
      summary = "프로젝트 투표",
      description = "참가자만 투표할 수 있으며, 사용자당 1표만 가능합니다. "
          + "본인 팀 프로젝트에는 투표할 수 없고, 어드민이 설정한 투표 시간에만 가능합니다.")
  @PostMapping
  public ResponseEntity<SuccessResponse<Void>> submit(
      @AuthUser AuthPrincipal principal,
      @Valid @RequestBody VoteSubmitRequest request
  ) {
    voteFacade.submit(principal, request);
    return ResponseEntity.ok(SuccessResponse.of("투표가 완료되었습니다."));
  }
}
