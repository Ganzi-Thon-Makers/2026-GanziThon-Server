package com.ganzithon.makers.platform.domain.stamp.presentation.dto;

public record StampCollectResponse(
    Long boothId,
    int collectedCount,
    int remainingToLuckyDraw,
    boolean luckyDrawEntered,
    boolean luckyDrawJustEntered
) {}
