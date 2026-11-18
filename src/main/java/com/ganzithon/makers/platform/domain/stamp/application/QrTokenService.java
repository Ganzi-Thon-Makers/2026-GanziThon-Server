package com.ganzithon.makers.platform.domain.stamp.application;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.stamp.domain.QrToken;
import com.ganzithon.makers.platform.domain.stamp.infrastructure.QrTokenRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QrTokenService {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final int TOKEN_BYTE_LENGTH = 32;
  private static final long EXPIRED_TOKEN_RETENTION_SECONDS = 1800L;

  private final QrTokenRepository qrTokenRepository;
  private final StampProperties stampProperties;

  @Transactional
  public QrToken issue(Long boothId) {
    LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(stampProperties.qrTokenTtlSeconds());
    QrToken qrToken = QrToken.issue(boothId, generateToken(), expiresAt);
    return qrTokenRepository.save(qrToken);
  }

  public QrToken validate(String token) {
    QrToken qrToken = qrTokenRepository.findByToken(token)
        .orElseThrow(() -> new BusinessException(ErrorCode.QR_TOKEN_NOT_FOUND));

    if (!qrToken.isValidAt(LocalDateTime.now(), stampProperties.qrTokenGraceSeconds())) {
      throw new BusinessException(ErrorCode.QR_TOKEN_EXPIRED);
    }

    return qrToken;
  }

  @Transactional
  public void deleteExpiredTokens() {
    LocalDateTime threshold = LocalDateTime.now()
        .minusSeconds(stampProperties.qrTokenGraceSeconds() + EXPIRED_TOKEN_RETENTION_SECONDS);
    qrTokenRepository.deleteByExpiresAtBefore(threshold);
  }

  private String generateToken() {
    byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
    SECURE_RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
