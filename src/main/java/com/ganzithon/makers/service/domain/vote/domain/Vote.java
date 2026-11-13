package com.ganzithon.makers.service.domain.vote.domain;

import com.ganzithon.makers.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "vote", uniqueConstraints = {
    @UniqueConstraint(name = "uk_vote_user_id", columnNames = "user_id")
})
public class Vote extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    private Vote(Long userId, Long projectId) {
        this.userId = userId;
        this.projectId = projectId;
    }

    public static Vote create(Long userId, Long projectId) {
        return new Vote(userId, projectId);
    }
}
