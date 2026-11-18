package com.ganzithon.makers.platform.domain.stamp.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrTokenCleanupScheduler {

  private static final long FIXED_RATE_MS = 60_000L;

  private final QrTokenService qrTokenService;

  @Scheduled(fixedRate = FIXED_RATE_MS)
  public void cleanupExpiredTokens() {
    try {
      qrTokenService.deleteExpiredTokens();
    } catch (Exception e) {
      log.warn("[QR-TOKEN-CLEANUP] 만료 토큰 삭제 실패", e);
    }
  }
}
