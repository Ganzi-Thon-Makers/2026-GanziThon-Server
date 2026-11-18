package com.ganzithon.makers.platform.domain.booth.application.dto;

import com.ganzithon.makers.platform.domain.booth.domain.Booth;

public record BoothInfo(
        Long boothId,
        String zone,
        String roomNumber,
        Integer boothNumber
) {
    public static BoothInfo from(Booth booth) {
        return new BoothInfo(
                booth.getId(),
                booth.getZone().name(),
                booth.getRoomNumber(),
                booth.getBoothNumber()
        );
    }
}
