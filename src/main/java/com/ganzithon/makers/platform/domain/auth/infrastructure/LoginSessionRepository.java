package com.ganzithon.makers.platform.domain.auth.infrastructure;

import com.ganzithon.makers.platform.domain.auth.domain.LoginSession;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoginSessionRepository extends JpaRepository<LoginSession, Long> {

    boolean existsBySessionId(String sessionId);

    Optional<LoginSession> findByRefreshTokenHash(String refreshTokenHash);

    @Modifying
    @Query("delete from LoginSession s where s.sessionId = :sessionId")
    void deleteBySessionId(@Param("sessionId") String sessionId);

    @Modifying
    @Query("delete from LoginSession s where s.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("delete from LoginSession s where s.expiresAt < :now")
    void deleteExpired(@Param("now") LocalDateTime now);
}
