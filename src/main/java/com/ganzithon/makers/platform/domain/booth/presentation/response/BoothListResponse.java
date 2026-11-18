package com.ganzithon.makers.platform.domain.booth.presentation.response;

import com.ganzithon.makers.platform.domain.booth.application.dto.BoothInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record BoothListResponse(
        Map<String, List<BoothResponse>> booths
) {

    public static BoothListResponse from(List<BoothInfo> boothInfos) {

        Map<String, List<BoothResponse>> groupedBooths =
                boothInfos.stream()
                        .collect(Collectors.groupingBy(
                                BoothInfo::zone,
                                Collectors.mapping(
                                        BoothResponse::from,
                                        Collectors.toList()
                                )
                        ));

        return new BoothListResponse(groupedBooths);
    }

    public record BoothResponse(
            Long boothId,
            String roomNumber,
            Integer boothNumber
    ) {
        public static BoothResponse from(BoothInfo info) {
            return new BoothResponse(
                    info.boothId(),
                    info.roomNumber(),
                    info.boothNumber()
            );
        }
    }
}
