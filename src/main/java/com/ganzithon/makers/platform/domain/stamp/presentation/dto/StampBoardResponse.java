package com.ganzithon.makers.platform.domain.stamp.presentation.dto;

import com.ganzithon.makers.platform.domain.booth.domain.Zone;
import java.time.LocalDateTime;
import java.util.List;

public record StampBoardResponse(
    int collectedCount,
    int remainingToLuckyDraw,
    boolean luckyDrawEntered,
    List<CollectedStamp> stamps
) {

  public record CollectedStamp(
      Long boothId,
      Zone zone,
      Integer boothNumber,
      LocalDateTime collectedAt
  ) {}
}
