package com.ganzithon.makers.platform.domain.stamp.presentation.dto;

public record StampCollectResponse(
    int collectedCount,
    int remainingToLuckyDraw,
    boolean luckyDrawEntered,
    boolean luckyDrawJustEntered
) {}
