package com.ganzithon.makers.platform.domain.stamp.presentation;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.common.security.AuthUser;
import com.ganzithon.makers.platform.domain.stamp.application.StampFacade;
import com.ganzithon.makers.platform.domain.stamp.presentation.dto.QrTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/qr-tokens")
@RequiredArgsConstructor
public class QrTokenController implements QrTokenApi {

  private final StampFacade stampFacade;

  @Override
  @PostMapping
  public ResponseEntity<SuccessResponse<QrTokenResponse>> issue(
      @AuthUser AuthPrincipal principal) {
    return ResponseEntity.ok(SuccessResponse.of(stampFacade.issueQrToken(principal.userId())));
  }
}
