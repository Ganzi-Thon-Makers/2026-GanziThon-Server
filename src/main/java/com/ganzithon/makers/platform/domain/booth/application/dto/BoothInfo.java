package com.ganzithon.makers.platform.domain.booth.application.dto;

import com.ganzithon.makers.platform.domain.booth.domain.Booth;

public record BoothInfo(
        Long boothId,
        String zone,
        String roomNumber,
        Integer boothNumber,
        Integer teamNumber
) {
    public static BoothInfo of(Booth booth, Integer teamNumber) {
        return new BoothInfo(
                booth.getId(),
                booth.getZone().name(),
                booth.getRoomNumber(),
                booth.getBoothNumber(),
                teamNumber
        );
    }
}
