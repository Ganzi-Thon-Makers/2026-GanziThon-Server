package com.ganzithon.makers.platform.domain.project.domain;

import com.ganzithon.makers.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "project")
public class Project extends BaseEntity {

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "team_name", length = 100, nullable = false)
    private String teamName;

    @Column(name = "summary", length = 200, nullable = false)
    private String summary;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "service_url", length = 500)
    private String serviceUrl;

    @Column(name = "thumbnail_url", length = 500, nullable = false)
    private String thumbnailUrl;

    @Column(name = "tech_stack", length = 500, nullable = false)
    private String techStack;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", length = 20, nullable = false)
    private Platform platform;

    private Project(
        String title,
        String teamName,
        String summary,
        String description,
        String serviceUrl,
        String thumbnailUrl,
        String techStack,
        Platform platform
    ) {
        this.title = title;
        this.teamName = teamName;
        this.summary = summary;
        this.description = description;
        this.serviceUrl = serviceUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.techStack = techStack;
        this.platform = platform;
    }

    public static Project create(
        String title,
        String teamName,
        String summary,
        String description,
        String serviceUrl,
        String thumbnailUrl,
        String techStack,
        Platform platform
    ) {
        return new Project(
            title,
            teamName,
            summary,
            description,
            serviceUrl,
            thumbnailUrl,
            techStack,
            platform
        );
    }
}
