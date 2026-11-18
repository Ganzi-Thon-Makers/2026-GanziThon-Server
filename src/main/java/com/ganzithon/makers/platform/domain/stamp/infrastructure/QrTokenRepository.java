package com.ganzithon.makers.platform.domain.stamp.infrastructure;

import com.ganzithon.makers.platform.domain.stamp.domain.QrToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrTokenRepository extends JpaRepository<QrToken, Long> {

  Optional<QrToken> findByToken(String token);

  void deleteByExpiresAtBefore(LocalDateTime threshold);
}
