package com.ganzithon.makers.platform.domain.stamp.presentation.dto;

import java.time.LocalDateTime;

public record QrTokenResponse(
    String token,
    LocalDateTime expiresAt,
    long ttlSeconds
) {}
