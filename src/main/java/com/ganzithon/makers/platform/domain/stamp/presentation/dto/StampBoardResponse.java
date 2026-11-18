package com.ganzithon.makers.platform.domain.stamp.presentation.dto;

import com.ganzithon.makers.platform.domain.booth.domain.Zone;
import java.time.LocalDateTime;
import java.util.List;

public record StampBoardResponse(
    int collectedCount,
    int remainingToLuckyDraw,
    boolean luckyDrawEntered,
    List<ZoneStatus> zoneStatuses
) {

  public record ZoneStatus(
      Zone zone,
      int totalBooths,
      int collectedCount,
      int remainingCount,
      boolean completed,
      List<BoothStamp> booths
  ) {}

  public record BoothStamp(
      Long boothId,
      String boothName,
      Integer boothNumber,
      boolean collected,
      LocalDateTime collectedAt
  ) {}
}
