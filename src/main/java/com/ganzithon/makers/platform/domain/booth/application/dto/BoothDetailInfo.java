package com.ganzithon.makers.platform.domain.booth.application.dto;

import com.ganzithon.makers.platform.domain.project.domain.Project;

public record BoothDetailInfo(
        Long projectId,
        String platform,
        String teamName,
        String title,
        String summary,
        String thumbnailUrl
) {
    public static BoothDetailInfo from(Project project) {
        return new BoothDetailInfo(
                project.getId(),
                project.getPlatform().name(),
                project.getTeamName(),
                project.getTitle(),
                project.getSummary(),
                project.getThumbnailUrl()
        );
    }
}
