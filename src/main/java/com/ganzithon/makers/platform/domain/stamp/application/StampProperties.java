package com.ganzithon.makers.platform.domain.stamp.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stamp")
public record StampProperties(
    long qrTokenTtlSeconds,
    long qrTokenGraceSeconds,
    long recollectCooldownSeconds,
    int zoneCompletionThreshold
) {}
