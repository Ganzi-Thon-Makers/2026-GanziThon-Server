package com.ganzithon.makers.platform.domain.project.domain;

import com.ganzithon.makers.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
// team_number 는 null 허용이지만, MySQL UNIQUE 는 NULL 끼리는 중복으로 보지 않아 팀 번호가 없는 프로젝트는 여러 개 가능
@Table(name = "project", uniqueConstraints = {
    @UniqueConstraint(name = "uk_project_team_number", columnNames = "team_number")
})
public class Project extends BaseEntity {

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "team_name", length = 100, nullable = false)
    private String teamName;

    @Column(name = "team_number")
    private Integer teamNumber;

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
        Integer teamNumber,
        String summary,
        String description,
        String serviceUrl,
        String thumbnailUrl,
        String techStack,
        Platform platform
    ) {
        this.title = title;
        this.teamName = teamName;
        this.teamNumber = teamNumber;
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
        return create(
            title,
            teamName,
            null,
            summary,
            description,
            serviceUrl,
            thumbnailUrl,
            techStack,
            platform
        );
    }

    public static Project create(
        String title,
        String teamName,
        Integer teamNumber,
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
            teamNumber,
            summary,
            description,
            serviceUrl,
            thumbnailUrl,
            techStack,
            platform
        );
    }

    public void update(
        String title,
        String teamName,
        Integer teamNumber,
        String summary,
        String description,
        String serviceUrl,
        String techStack,
        Platform platform
    ) {
        this.title = title;
        this.teamName = teamName;
        this.teamNumber = teamNumber;
        this.summary = summary;
        this.description = description;
        this.serviceUrl = serviceUrl;
        this.techStack = techStack;
        this.platform = platform;
    }

    public void changeThumbnail(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
