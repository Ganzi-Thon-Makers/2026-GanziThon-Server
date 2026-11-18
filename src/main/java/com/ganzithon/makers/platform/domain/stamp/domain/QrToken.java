package com.ganzithon.makers.platform.domain.stamp.domain;

import com.ganzithon.makers.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "qr_token", uniqueConstraints = {
    @UniqueConstraint(name = "uk_qr_token_token", columnNames = "token")
})
public class QrToken extends BaseEntity {

    @Column(name = "booth_id", nullable = false)
    private Long boothId;

    @Column(name = "token", length = 64, nullable = false)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    private QrToken(Long boothId, String token, LocalDateTime expiresAt) {
        this.boothId = boothId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public static QrToken issue(Long boothId, String token, LocalDateTime expiresAt) {
        return new QrToken(boothId, token, expiresAt);
    }

    public boolean isValidAt(LocalDateTime time, long graceSeconds) {
        return time.isBefore(expiresAt.plusSeconds(graceSeconds));
    }
}
