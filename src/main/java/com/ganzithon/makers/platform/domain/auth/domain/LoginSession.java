package com.ganzithon.makers.platform.domain.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "login_session",
        indexes = {
                @Index(name = "idx_login_session_sid", columnList = "sessionId", unique = true),
                @Index(name = "idx_login_session_user", columnList = "userId"),
                @Index(name = "idx_login_session_refresh", columnList = "refreshTokenHash", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 36)
    private String sessionId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false, length = 64)
    private String refreshTokenHash;

    @Column(nullable = false)
    private LocalDateTime refreshExpiresAt;

    public LoginSession(
            Long userId,
            String sessionId,
            LocalDateTime expiresAt,
            String refreshTokenHash,
            LocalDateTime refreshExpiresAt
    ) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.expiresAt = expiresAt;
        this.refreshTokenHash = refreshTokenHash;
        this.refreshExpiresAt = refreshExpiresAt;
    }

    public boolean isRefreshExpired(LocalDateTime now) {
        return refreshExpiresAt.isBefore(now);
    }

    public void rotateRefreshToken(String newRefreshTokenHash, LocalDateTime newRefreshExpiresAt) {
        this.refreshTokenHash = newRefreshTokenHash;
        this.refreshExpiresAt = newRefreshExpiresAt;
    }

}
