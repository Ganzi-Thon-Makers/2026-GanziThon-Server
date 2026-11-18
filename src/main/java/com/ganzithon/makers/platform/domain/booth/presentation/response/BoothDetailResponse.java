package com.ganzithon.makers.platform.domain.booth.presentation.response;

import com.ganzithon.makers.platform.domain.booth.application.dto.BoothDetailInfo;

public record BoothDetailResponse(
        Long projectId,
        String platform,
        String teamName,
        String title,
        String summary,
        String thumbnailUrl
) {
    public static BoothDetailResponse from(BoothDetailInfo info) {
        return new BoothDetailResponse(
                info.projectId(),
                info.platform(),
                info.teamName(),
                info.title(),
                info.summary(),
                info.thumbnailUrl()
        );
    }
}
