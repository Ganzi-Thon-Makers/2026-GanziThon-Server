package com.ganzithon.makers.platform.domain.stamp.presentation;

import com.ganzithon.makers.common.auth.CurrentUserProvider;
import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.platform.domain.stamp.application.StampFacade;
import com.ganzithon.makers.platform.domain.stamp.presentation.dto.QrTokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/qr-tokens")
@RequiredArgsConstructor
public class QrTokenController {

  private final StampFacade stampFacade;

  @PostMapping
  public ResponseEntity<SuccessResponse<QrTokenResponse>> issue(
      @RequestParam Long userId) {
    return ResponseEntity.ok(SuccessResponse.of(stampFacade.issueQrToken(userId)));
  }
}
