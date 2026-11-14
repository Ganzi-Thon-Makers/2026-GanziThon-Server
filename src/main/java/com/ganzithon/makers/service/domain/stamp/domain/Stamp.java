package com.ganzithon.makers.service.domain.stamp.domain;

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
@Table(name = "stamp", uniqueConstraints = {
    @UniqueConstraint(name = "uk_stamp_user_booth", columnNames = {"user_id", "booth_id"})
})
public class Stamp extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "booth_id", nullable = false)
    private Long boothId;

    private Stamp(Long userId, Long boothId) {
        this.userId = userId;
        this.boothId = boothId;
    }

    public static Stamp create(Long userId, Long boothId) {
        return new Stamp(userId, boothId);
    }
}
