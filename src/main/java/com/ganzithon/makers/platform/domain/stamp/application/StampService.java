package com.ganzithon.makers.platform.domain.stamp.application;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.stamp.domain.Stamp;
import com.ganzithon.makers.platform.domain.stamp.infrastructure.StampRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StampService {

  private final StampRepository stampRepository;
  private final StampProperties stampProperties;

  @Transactional
  public Stamp collect(Long userId, Long boothId) {
    if (stampRepository.existsByUserIdAndBoothId(userId, boothId)) {
      throw new BusinessException(ErrorCode.STAMP_ALREADY_COLLECTED);
    }

    stampRepository.findTopByUserIdOrderByCreatedAtDesc(userId).ifPresent(last -> {
      LocalDateTime cooldownUntil = last.getCreatedAt().plusSeconds(stampProperties.recollectCooldownSeconds());
      if (LocalDateTime.now().isBefore(cooldownUntil)) {
        throw new BusinessException(ErrorCode.STAMP_COOLDOWN);
      }
    });

    try {
      return stampRepository.save(Stamp.create(userId, boothId));
    } catch (DataIntegrityViolationException e) {
      throw new BusinessException(ErrorCode.STAMP_ALREADY_COLLECTED);
    }
  }

  public long countByUserId(Long userId) {
    return stampRepository.countByUserId(userId);
  }

  public List<Stamp> findAllByUserId(Long userId) {
    return stampRepository.findAllByUserId(userId);
  }
}
