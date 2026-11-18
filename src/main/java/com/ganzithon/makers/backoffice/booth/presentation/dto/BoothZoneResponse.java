package com.ganzithon.makers.backoffice.booth.presentation.dto;

import com.ganzithon.makers.platform.domain.booth.domain.Zone;
import java.util.List;

public record BoothZoneResponse(
    Zone zone,
    int assignedCount,
    int totalCount,
    List<Room> rooms
) {

  public record Room(
      String roomNumber,
      List<BoothItem> booths
  ) {
  }

  // project: 배정된 프로젝트, 비어 있으면 null
  public record BoothItem(
      Long boothId,
      Integer boothNumber,
      BoothProjectResponse project
  ) {
  }
}
