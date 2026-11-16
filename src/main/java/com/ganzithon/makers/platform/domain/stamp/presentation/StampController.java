package com.ganzithon.makers.platform.domain.stamp.presentation;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.common.security.AuthUser;
import com.ganzithon.makers.platform.domain.stamp.application.StampFacade;
import com.ganzithon.makers.platform.domain.stamp.presentation.dto.StampBoardResponse;
import com.ganzithon.makers.platform.domain.stamp.presentation.dto.StampCollectRequest;
import com.ganzithon.makers.platform.domain.stamp.presentation.dto.StampCollectResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stamps")
@RequiredArgsConstructor
public class StampController {

  private final StampFacade stampFacade;

  @PostMapping
  public ResponseEntity<SuccessResponse<StampCollectResponse>> collect(
      @AuthUser AuthPrincipal principal,
      @Valid @RequestBody StampCollectRequest requestBody) {
    return ResponseEntity.ok(SuccessResponse.of(stampFacade.collect(principal.userId(), requestBody.token())));
  }

  @GetMapping
  public ResponseEntity<SuccessResponse<StampBoardResponse>> getBoard(
      @AuthUser AuthPrincipal principal) {
    return ResponseEntity.ok(SuccessResponse.of(stampFacade.getBoard(principal.userId())));
  }
}
