package com.ganzithon.makers.platform.domain.stamp.infrastructure;

import com.ganzithon.makers.platform.domain.stamp.domain.Stamp;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StampRepository extends JpaRepository<Stamp, Long> {

  boolean existsByUserIdAndBoothId(Long userId, Long boothId);

  List<Stamp> findAllByUserId(Long userId);

  long countByUserId(Long userId);

  Optional<Stamp> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
